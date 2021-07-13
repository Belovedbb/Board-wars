import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {
  CFD,
  CFDChartData,
  CFDData2,
  CFDResponse,
  Column,
  ColumnResponse,
  Project,
  ProjectResponse
} from "../../kanban-model";
import {TaskService} from "../../board/services/task-service";
import {Observable, of} from "rxjs";
import {filter, map, mergeAll, toArray} from "rxjs/operators";
import {ProjectService} from "../../board/services/project-service";
import {flatMap} from "rxjs/internal/operators";
import * as Highcharts from "highcharts";

@Injectable({ providedIn: 'root' })
export class CfdChartService extends RestService<CFDResponse> {

  constructor(injector: Injector, private projectService: ProjectService) {
    super(CFDResponse, 'kanban/graphics/cumulative-flow-diagram/project', injector);
  }

  getAllProjects(): Observable<Project[]> {
      return this.projectService.getAllProjects()
  }

  getProject(code: number) : Observable<Project> {
    return this.projectService.get(code).pipe(
      filter((projectResponse) => projectResponse.success === true),
      map((projectResponse: ProjectResponse) => this.projectService.loadProjectToDomain(projectResponse.body)));
  }

  getDataOption(projectCode: number): Observable<object> {
    return this.getSelectedProjectCFDData(projectCode)
      .pipe(map((chartData: CFDChartData[]) => {
        let currentMonth = -1;
        return {
          chart: {
            type: 'area',
            zoomType: 'x',
            panning: true,
            panKey: 'shift',
            resetZoomButton: {
              relativeTo: 'spacingBox',
              position: {
                y: 0,
                x: 0
              },
              theme: {
                fill: 'white',
                'stroke-width': 1,
                stroke: 'grey',
                r: 0,
                states: {
                  hover: {
                    fill: '#b7cfec'
                  },
                  select: {
                    stroke: '#039',
                    fill: '#b7cfec'
                  }
                }
              }
            }
          },
          title: {
            text: ''
          },
          xAxis: {
            title: {
              text: ''
            },
            labels: {
              style: {
                fontWeight: 'normal',
                fontSize: '12px',
                fontFamily: 'Segoe UI',
                color: 'black'
              },
              formatter: function () {
                let localDate = new Date(this.value);
                let addLabel = false;
                if (currentMonth != localDate.getMonth())
                {
                  addLabel = true;
                  currentMonth = localDate.getMonth();
                }

                if (this.isFirst) {
                  return Highcharts.dateFormat('%d', this.value) + '<br/>' + Highcharts.dateFormat('%b', this.value);
                }
                if (addLabel) {
                  return Highcharts.dateFormat('%d', this.value) + '<br/>' + Highcharts.dateFormat('%b', this.value);
                }
                else {
                  return Highcharts.dateFormat('%d', this.value);
                }
              }
            },
            type: 'datetime',
            tickInterval: 24 * 3600 * 1000 * 5
          },
          credits: {
            enabled: false
          },
          yAxis: {
            title: {
              text: 'Task Count',
              style: {
                fontWeight: 'normal',
                fontSize: '12px',
                fontFamily: 'Segoe UI',
                color: 'black'
              }
            },
            labels: {
              style: {
                fontWeight: 'normal',
                fontSize: '12px',
                fontFamily: 'Segoe UI',
                color: 'black'
              }
            },
            tickInterval: 5
          },
          legend: {
            enabled: false,
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'top',
            itemStyle: {
              fontWeight: 'normal',
              fontSize: '12px',
              fontFamily: 'Segoe UI'
            }
          },
          tooltip: {
            crosshairs: true,
            shared: true
          },
          plotOptions: {
            area: {
              stacking: 'normal',
              lineColor: '#666666',
              lineWidth: 0,
              marker: { enabled: false }
            }
          },
          series: chartData,
          exporting: {
            enabled: false
          }
        };
      }));
  }


  private getSelectedProjectCFDData (projectCode: number): Observable<CFDChartData[]> {
      let proj = new Project();
      proj.code = projectCode;
    return this.projectService.getFilteredProject(proj).pipe(
      flatMap((project: Project) => this.get(project.code).pipe(map(cfdResponse => cfdResponse.body))),
      map((cfd: CFD) => this.mapCFDToCFDData(cfd))
    )
  }

  private mapCFDToCFDData(cfd: CFD): CFDChartData[] {
    let chartData: CFDChartData[]  = [];
    function groupBy(xs, f) {
      return xs.reduce((r, v, i, a, k = f(v)) => ((r[k] || (r[k] = [])).push(v), r), {});
    }
    const groups = groupBy(cfd.flows, (c) => c.column.name);
    chartData.push(...this.assignTitle(groups));
    chartData.push(...this.assignData(groups));
    return chartData;
  }

  private assignTitle(groups): CFDChartData[]{
    let c : CFDChartData[]  = [];
    for(let key of Object.keys(groups)) {
      let values = groups[key];
      const column = this.getColumnValue(values);
      let data = new CFDChartData();
      data.name = key;
      data.color = column.color;
      data.type = 'scatter';
      data.marker = { symbol: 'circle' };
      c.push(data);
    }
    return c;
  }

  private assignData(groups): CFDChartData[] {
    let keys = Object.keys(groups);
    let c : CFDChartData[]  = [];
    for(let key of keys) {
      let values = groups[key];
      const column = this.getColumnValue(values);
      let data = new CFDChartData();
      data.name = key;
      data.color = column.color;
      data.data = this.getCFDData2(values);
      data.marker = { symbol: 'circle' };
      data.showInLegend = false;
      c.push(data);
    }
    return c;
  }

  private getColumnValue(values) : Column {
    let column: Column = null;
    if(values && values.length > 0) {
      column = values[0].column;
    }
    return column;
  }

  private getCFDData2(data): any {
    let cfdData2: any[] = [];
    if(data && data.length > 0) {
      for(let data2 of data) {
        let cfd: CFDData2 = new CFDData2();
        cfd.currentTime = data2.currentTime;
        cfd.taskSize = data2.taskSize;
        cfdData2.push([Number(new Date(data2.currentTime)), data2.taskSize + 1]);
      }
    }
    return cfdData2;
  }

  getAllCFDs() : Observable<CFD[]> {
    return this.getAll()
      .pipe(
        mergeAll(),
        filter(response => response.success),
        map((response: CFDResponse) => {
          return response.body;
        }),
        toArray()
      );
  }

}

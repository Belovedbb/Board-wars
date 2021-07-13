import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, AfterViewChecked } from '@angular/core';
import * as Highcharts from 'highcharts/highcharts-gantt';
import {Observable} from "rxjs";
import {Column, Gantt, Project} from "../../kanban-model";
import {GanttChartService} from "./gantt-chart.service";
import {map} from "rxjs/operators";

@Component({
  selector: 'kanban-gantt-chart',
  templateUrl: './gantt-chart.component.html',
  styleUrls: ['./gantt-chart.component.scss']
})
export class GanttChartComponent implements OnInit,AfterViewInit {
  @ViewChild('divRef', { static: false }) divReference: ElementRef;

  projects: Observable<Project[]> = null;
  selectedProject: Observable<Project> = null;
  columns: Column[] = [];
  selectedColumn: Column = null;

  constructor(private ganttChartService: GanttChartService) {
    this.projects = this.ganttChartService.getAllProjects();
  }


  ngAfterViewInit() {

  }

  ngOnInit(): void {

  }

  showGanttChart(project: string, column: string,  data: object[]) {
    Highcharts.ganttChart(this.divReference.nativeElement as HTMLElement, {
      title: {
        text: project + ' Chart'
      },
      chart: { renderTo: this.divReference.nativeElement as HTMLElement },
      series: [
        {
          name: column,
          type: 'gantt',
          data: data
        }]
    });
  }

  onProjectChange(code: number): void {
    this.selectedProject = this.ganttChartService.getProject(code);
    this.selectedColumn = null;
    this.selectedProject.pipe(
      map(project => {
        this.columns = project.columns;
      })
    ).subscribe();
  }

  getColumn( value):Column {
    return  this.columns.find(obj => {
      return obj.name === value
    })

  }
  onColumnChange(name: string) : void {
    this.selectedProject.pipe(
      map(project => {
        let column = this.getColumn(name);
        let ganttData = [];
        if(column && column.tasks && column.tasks.length > 0) {
          for (let i = 0; i < column.tasks.length; i++) {
            let t = column.tasks[i];
            let gantt = new Gantt();
            gantt.id = '' + t.position;
            gantt.name = t.name;
            gantt.start = new Date(t.startDate);
            gantt.end = new Date(t.endDate);
            if(i > 0) {
              gantt.dependency = "" + column.tasks[i - 1].position;
            }
            ganttData.push(gantt);
          }
        }
        this.showGanttChart(project.name, name, ganttData);
      })
    ).subscribe()
  }
}

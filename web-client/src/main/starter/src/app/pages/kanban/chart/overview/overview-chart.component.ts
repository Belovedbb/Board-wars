import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NbThemeService} from "@nebular/theme";
import {Column, Gantt, OverviewData, OverviewOrder, Project} from "../../kanban-model";
import {OverviewChartService} from "./overview-chart.service";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";
import {TaskSubBoardComponent} from "../../board/task-sub/task-sub-board.component";


@Component({
  selector: 'kanban-overview-chart',
  styleUrls: ['./overview-chart.component.scss'],
  templateUrl: './overview-chart.component.html',
})
export class OverviewChartComponent implements OnDestroy {
  data: any;
  options: any;
  projects: Observable<Project[]> = null;
  selectedProject: Observable<Project> = null;
  orders: OverviewOrder[] = [];
  selectedOrder: OverviewOrder = null;

  constructor(private overviewChartService: OverviewChartService) {
    this.projects = this.overviewChartService.getAllProjects().pipe(map((proj: Project[]) => this.mapProjects(proj)));
  }

  mapProjects(ele: Project[]) : Project[] {
    if(ele && ele.length > 0) {
      let project : Project = ele[0];
      this.selectedProject = of(project);
      this.populateDependencies(project);
    }
    return ele;
  }

  populateDependencies(project: Project): void {
    this.orders = this.populateOrder(project);
    if(this.orders.length > 1){
      this.selectedOrder = this.orders[0];
      this.createChart(project, this.orders[0]);
    }else{
      this.selectedOrder = null;
    }

  }

  populateOrder(project: Project): OverviewOrder[] {
    let overviewOrders = [];
    if(project) {
      if(project.columns && project.columns.length > 0){
        overviewOrders.push({'key': 'column', 'type': 'Column'});
        if(this.hasTag(project.columns)){
          overviewOrders.push({'key': 'tag', 'type': 'Tag'})
        }
        if(this.hasCategory(project.columns)){
          overviewOrders.push({'key': 'category', 'type': 'Category'})
        }
      }
    }
    return overviewOrders;
  }

  hasTag(columns: Column[]): boolean {
    //all tags for number of task
    let condition = false;
    for(const c of columns) {
      if(c) {
        for(const task of c.tasks) {
          if(task && task.tags && task.tags.length > 0)  return true;
        }
      }
    }
    return condition;
  }

  hasCategory(columns: Column[]): boolean {
    //will always have category
    return true;
  }

  createChart(project: Project, order: OverviewOrder): void {
    let labels: string[] = [];
    let data: any[] = [];
    let colors: string[] = [];
    switch (order.key) {
      case 'column': {
        for(let c of this.getColumnChartData(project) ) {
          labels.push(c.label);
          data.push(c.value);
          colors.push(c.color);
        }
        break;
      }
      case 'tag': {
        for(let c of this.getTagChartData(project) ) {
          labels.push(c.label);
          data.push(c.value);
          colors.push(c.color);
        }
        break;
      }
      case 'category': {
        for(let c of this.getCategoryChartData(project) ) {
          labels.push(c.label);
          data.push(c.value);
          colors.push(c.color);
        }
        break;
      }
    }
    this.data = {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: colors,
      }],
    };

    this.options = {
      maintainAspectRatio: false,
      responsive: true,
      scales: {
        xAxes: [
          {
            display: false,
          },
        ],
        yAxes: [
          {
            display: false,
          },
        ],
      },
      legend: {
        labels: {
          fontColor: 'black',
        },
      },
    };
  }

  getColumnChartData(project: Project): OverviewData[] {
    let overviewData: OverviewData[] = [];
    for(let column of project.columns) {
      let data: OverviewData = new OverviewData();
      data.value = column.tasks ? column.tasks.length : 0;
      data.color = column.color;
      data.label = column.name;
      overviewData.push(data);
    }
    return overviewData;
  }

  getTagChartData(project: Project): OverviewData[] {
    let data: OverviewData[] = [];
    let container = [];
    for(let column of project.columns) {
        for(let task of column.tasks) {
          container.push(...task.tags);
        }
    }
    const tags = this.reduceObject(container);
    for(let key of Object.keys(tags)) {
      let val: OverviewData = new OverviewData();
      let keyValue = tags[key];
      val.label = key;
      val.value = keyValue;
      val.color = this.getRandomColor();
      data.push(val);
    }
    return data;
  }

  getCategoryChartData(project: Project): OverviewData[] {
    let data: OverviewData[] = [];
    let container = [];
    for(let column of project.columns) {
      for(let task of column.tasks) {
        container.push(task.categories[0]);
      }
    }
    const result = this.reduceObject(container);
    for(let key of Object.keys(result)) {
      let val: OverviewData = new OverviewData();
      let keyValue = result[key];
      val.label = key;
      val.value = keyValue;
      val.color = this.findCategoryColor(key);
      data.push(val);
    }
    return data;
  }

  getRandomColor(): string {
    let letters = '0123456789ABCDEF'.split('');
    let color = '#';
    for (let i = 0; i < 6; i++ ) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  }

  findCategoryColor(val: string): string {
    return TaskSubBoardComponent.buildCategories().find(obj => {
      return obj.part.valueOf() === val;
    }).color;
  }

  reduceObject(container) : any {
    return  container.reduce((acc, curr) => (acc[curr] = (acc[curr] || 0) + 1, acc), {});
  }

  onProjectChange(code: number): void {
    this.selectedProject = this.overviewChartService.getProject(code).pipe(map(project => {
      this.populateDependencies(project);
      return project;
    }));
  }

  onOrderChangeChange(columnName: string) : void {
    this.selectedProject.pipe(
      map(project => {
        let  order = this.orders.find(obj => {
          return obj.key === columnName
        });
        this.selectedOrder = order;
        this.createChart(project, order);
      })
    ).subscribe()
  }

  ngOnDestroy(): void {
  }
}

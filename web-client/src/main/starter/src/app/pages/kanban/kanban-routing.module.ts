import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import {KanbanComponent} from "./kanban.component";
import {KanbanBoardComponent} from "./board/board.component";
import {KanbanChartComponent} from "./chart/chart.component";

const routes: Routes = [{
  path: '',
  component: KanbanComponent,
  children: [{
    path: 'board',
    component: KanbanBoardComponent,
  },{
    path: 'chart',
    component: KanbanChartComponent,
  }],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class KanbanRoutingModule { }

export const routedComponents = [
  KanbanComponent,
  KanbanChartComponent,
  KanbanBoardComponent
];

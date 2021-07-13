import { NgModule } from '@angular/core';
import {
  NbAlertModule,
  NbButtonModule,
  NbCardModule, NbChatModule, NbDatepickerModule,
  NbIconModule,
  NbInputModule, NbListModule, NbPopoverModule, NbRadioModule,
  NbSelectModule,
  NbTagModule, NbTimepickerModule, NbTooltipModule
} from '@nebular/theme';

import { ThemeModule } from '../../@theme/theme.module';

import { KanbanRoutingModule, routedComponents } from './kanban-routing.module';
import {DragDropModule} from "@angular/cdk/drag-drop";
import {SubBoardComponent} from "./board/sub/sub-board.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ColumnSubBoardComponent} from "./board/column-sub/column-sub-board.component";
import {TaskSubBoardComponent} from "./board/task-sub/task-sub-board.component";
import {CFDChartComponent} from "./chart/cfd/cfd-chart.component";
import {ChartModule} from "angular2-chartjs";
import {GanttChartComponent} from "./chart/gantt/gantt-chart.component";
import { OverviewChartComponent } from './chart/overview/overview-chart.component';

@NgModule({
  imports: [
    NbCardModule,
    ThemeModule,
    KanbanRoutingModule,
    DragDropModule,
    NbTagModule,
    NbIconModule,
    NbSelectModule,
    NbButtonModule,
    NbInputModule,
    NbAlertModule,
    NbDatepickerModule,
    NbTimepickerModule,
    NbRadioModule,
    ReactiveFormsModule,
    FormsModule,
    NbTooltipModule,
    NbPopoverModule,
    NbChatModule,
    ChartModule,
    NbListModule
  ],
  declarations: [
    ...routedComponents,
    SubBoardComponent,
    ColumnSubBoardComponent,
    TaskSubBoardComponent,
    CFDChartComponent,
    GanttChartComponent,
    OverviewChartComponent
  ],
})
export class KanbanModule { }


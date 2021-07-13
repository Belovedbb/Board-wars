import { NgModule } from '@angular/core';
import {
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbTabsetModule,
  NbUserModule,
  NbRadioModule,
  NbSelectModule,
  NbListModule,
  NbIconModule,
} from '@nebular/theme';
import { NgxEchartsModule } from 'ngx-echarts';

import { ThemeModule } from '../../@theme/theme.module';
import { DashboardComponent } from './dashboard.component';
import { StatusCardComponent } from './status-card/status-card.component';
import { FormsModule } from '@angular/forms';
import {HistoryComponent} from "./history/history.component";
import {CommonModule} from "@angular/common";
import { ClarityModule } from '@clr/angular';
import { HistoryScrumComponent } from './history/history-scrum/history-scrum.component';
import { HistoryKanbanComponent } from './history/history-kanban/history-kanban.component';
import { HistoryGraphicsComponent } from './history/history-graphics/history-graphics.component';
import {HistoryAllComponent} from "./history/history-all/history-all.component";
import { HistoryTimelineComponent } from './history/history-timeline/history-timeline.component';
import { ActivityFrequencyComponent } from './activity-frequency/activity-frequency.component';
import {ActivityFrequencyChartComponent} from "./activity-frequency/activity-frequency-chart/activity-frequency-chart.component";
import { HistoryManagementComponent } from './history/history-management/history-management.component';

@NgModule({
  imports: [
    ClarityModule,
    CommonModule,
    FormsModule,
    ThemeModule,
    NbCardModule,
    NbUserModule,
    NbButtonModule,
    NbTabsetModule,
    NbActionsModule,
    NbRadioModule,
    NbSelectModule,
    NbListModule,
    NbIconModule,
    NbButtonModule,
    NgxEchartsModule,
  ],
  declarations: [
    HistoryManagementComponent,
    HistoryTimelineComponent,
    HistoryScrumComponent,
    HistoryKanbanComponent,
    HistoryGraphicsComponent,
    HistoryAllComponent,
    HistoryComponent,
    DashboardComponent,
    StatusCardComponent,
    ActivityFrequencyComponent,
    ActivityFrequencyChartComponent,
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule { }

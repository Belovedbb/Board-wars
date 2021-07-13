import { Component, OnDestroy } from '@angular/core';
import { NbThemeService } from '@nebular/theme';

import { takeWhile } from 'rxjs/operators';
import { forkJoin } from 'rxjs';
import {ActivityFrequency, ActivityFrequencyChart, Month} from "../dashboard-model";
import {ActivityFrequencyService} from "./activity-frequency.service";

@Component({
  selector: 'dashboard-activity-frequency',
  styleUrls: ['./activity-frequency.component.scss'],
  templateUrl: './activity-frequency.component.html',
})
export class ActivityFrequencyComponent implements OnDestroy {

  private alive = true;

  listData: ActivityFrequency[] = null;
  chartData: ActivityFrequencyChart[] = null;

  type = 'Kanban';
  types = ['Kanban'];

  currentTheme: string;
  constructor(private activityFrequencyService: ActivityFrequencyService,
              private themeService: NbThemeService) {
    forkJoin(
      this.activityFrequencyService.getListData(),
      this.activityFrequencyService.getChartData(),
    )
      .subscribe(([listData, chartData]: [ActivityFrequency[], ActivityFrequencyChart[]] ) => {
        this.listData = listData;
        this.chartData = chartData;
      });
    this.themeService.getJsTheme()
      .pipe(takeWhile(() => this.alive))
      .subscribe(theme => {
        this.currentTheme = theme.name;
    });

  }

  getTaskTotal(listData: ActivityFrequency[]): number {
    return this.getActivityFreqTotal(listData, 'taskCount');
  }

  getProjectTotal(listData: ActivityFrequency[]): number {
    return this.getActivityFreqTotal(listData, 'projectCount');
  }

  getActivityFreqTotal(listData: ActivityFrequency[], field: string): number {
    return !listData || listData.length < 1 ? 0 : listData.reduce((a, b) => a + (this.sumMonthValue(field, b.months) || 0), 0);
  }

  sumMonthValue(key: string, months: Month[]): number {
    return !months || months.length < 1 ? 0 : months.reduce((a, b) => a + (Number(b[key]) || 0), 0);
  }

  ngOnDestroy() {
    this.alive = false;
  }
}

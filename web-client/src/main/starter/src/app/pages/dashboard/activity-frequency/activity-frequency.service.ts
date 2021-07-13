import {Injectable} from "@angular/core";
import {Observable, of, of as observableOf} from "rxjs";
import {
  ActivityFrequency,
  ActivityFrequencyChart,
  ActivityFrequencyData,
  ActivityFrequencyResponse, Month
} from "../dashboard-model";
import {ApiHttpService} from "../../../@core/utils";
import {Config} from "../../../config/config";
import {filter, map, mergeAll, toArray} from "rxjs/operators";
import {Column, ProjectResponse} from "../../kanban/kanban-model";
import {HateoasUtil} from "../../../@core/utils/hateoas.service";

@Injectable({ providedIn: 'root' })
export class ActivityFrequencyService extends ActivityFrequencyData {

  chartData: Observable<ActivityFrequencyChart[]> = of([]);

  constructor(private httpService: ApiHttpService) {
    super();
    this.chartData = this.httpService.
    get(Config.resolveParameterLink(Config.API_ROUTE.KANBAN_ACTIVITY_FREQUENCY_POINTS, "" + new Date().getFullYear()))
      .map(( data: number[]) => {
        return data.map((p, index) => {
          return (<ActivityFrequencyChart>{
            label: (index % 5 === 3) ? `${Math.round(index / 5)}` : '',
            value: p,
          })
        });
      });
  }

  loadActivityFrequencyToDomain(data: any, isEmbedded: boolean = false) : ActivityFrequencyResponse {
    if(!data) return null;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    return data;
  }

  loadActivityFrequencyArrayToDomain(data: any, isEmbedded: boolean = false) : ActivityFrequencyResponse[] {
    let activityFrequencies: ActivityFrequencyResponse[] = [];
    if(!data) return activityFrequencies;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    for(let activityFrequency of data) {
      activityFrequencies.push(this.loadActivityFrequencyToDomain(activityFrequency, true));
    }
    return activityFrequencies;
  }

  getListData(): Observable<ActivityFrequency[]> {
    return this.httpService.get(Config.resolveParameterLink(Config.API_ROUTE.KANBAN_ACTIVITY_FREQUENCY, "" + new Date().getFullYear()))
      .map(data => this.loadActivityFrequencyArrayToDomain(data, true))
      .pipe(
        mergeAll(),
        filter((response : ActivityFrequencyResponse)=> response.success),
        map(response => response.body ),
        toArray(),
        map(arr => this.linkTrend(arr))
      );
  }

  linkTrend(freqs: ActivityFrequency[]): ActivityFrequency[] {
    if(freqs && freqs.length > 0) {
      for(let freq of freqs){
        freq.active = true;
        freq.months = this.handleTrendMonth(freq.months);
      }
    }
    return freqs;
  }

  handleTrendMonth(months: Month[]): Month[] {
    if(months && months.length > 0) {
      months[0].down = false;
      let prevTaskNo = months[0].projectCount ? Number(months[0].projectCount) : 0;
      for(let i = 1; i < months.length; i++) {
        let curTaskNo = months[i].projectCount ? Number(months[i].projectCount) : 0;
        if(curTaskNo >= prevTaskNo)  months[i].down = false;
        else months[i].down = true;
        prevTaskNo = curTaskNo;
      }
    }
    return months;
  }

  getChartData(): Observable<ActivityFrequencyChart[]> {
    return this.chartData;
  }
}

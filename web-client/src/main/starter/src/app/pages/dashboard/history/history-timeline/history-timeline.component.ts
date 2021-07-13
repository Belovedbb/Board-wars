import '@clr/icons';
import '@clr/icons/shapes/all-shapes';
import { ClarityModule } from '@clr/angular';
import {Component, Input, OnInit} from "@angular/core";
import {TimeLineSnippet} from "../../dashboard-model";

@Component({
  selector: 'dashboard-history-timeline',
  styleUrls: ['./history-timeline.component.scss'],
  templateUrl: './history-timeline.component.html',
})
export class HistoryTimelineComponent implements OnInit {

  @Input() data : TimeLineSnippet[] = [];

  ngOnInit(): void {

  }

  getFormattedTag(tag: string, body: string) : string {
    if(!tag) return '';
    return '<' + tag + '>' + body + '</'+tag +'>';
  }

  getDateFormat(date: Date): string {
    if(!date) return '';
    if(typeof(date) == 'string' ){
      date = new Date(date);
    }
    let year = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(date);
    let month = new Intl.DateTimeFormat('en', { month: 'short' }).format(date);
    let day = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(date);
    return day+'-'+month+'-'+year;
  }

}

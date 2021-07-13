import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {TimeLineSnippet} from "../../dashboard-model";
import {HistoryGraphicsService} from "../history-graphics/history-graphics.service";

@Component({
  selector: 'dashboard-history-graphics',
  styleUrls: ['./history-graphics.component.scss'],
  templateUrl: './history-graphics.component.html',
})
export class HistoryGraphicsComponent implements OnInit, OnDestroy{
  private sseStream: Subscription;
  data: TimeLineSnippet[]  = [];
  page: number = 0;
  size: number = 10;
  loadedPrev: boolean = false;

  constructor(private historyGraphicsService: HistoryGraphicsService) {

  }

  ngOnInit(): void {
    this.sseStream = this.historyGraphicsService.registerGraphicsEvent(this.data);
  }

  ngOnDestroy() {
    if(this.sseStream) {
      this.sseStream.unsubscribe();
    }
    this.data = [];
  }

  loadNext() {
    if(this.loadedPrev) {
      ++this.page;
    }
    this.historyGraphicsService.getPersistentTimelineData(this.page, this.size)
      .subscribe((timelines: TimeLineSnippet[]) =>  {
        if(timelines && timelines.length > 0) this.loadedPrev = true;
        else this.loadedPrev = false;
        this.data.push(...timelines);
      });
  }

}

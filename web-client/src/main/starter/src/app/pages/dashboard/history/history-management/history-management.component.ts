import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {History, TimeLineSnippet, TimeLineSnippetBody, TimelineType} from "../../dashboard-model";
import {HistoryManagementService} from "../history-management/history-management.service";

declare var EventSource;

@Component({
  selector: 'dashboard-history-management',
  styleUrls: ['./history-management.component.scss'],
  templateUrl: './history-management.component.html',
})
export class HistoryManagementComponent implements OnInit, OnDestroy {
  private sseStream: Subscription;
  data: TimeLineSnippet[]  = [];
  page: number = 0;
  size: number = 10;
  loadedPrev: boolean = false;

  constructor(private historyManagementService: HistoryManagementService) {

  }

  ngOnInit(): void {
    this.sseStream = this.historyManagementService.registerManagementEvent(this.data);
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
    this.historyManagementService.getPersistentTimelineData(this.page, this.size)
      .subscribe((timelines: TimeLineSnippet[]) =>  {
        if(timelines && timelines.length > 0) this.loadedPrev = true;
        else this.loadedPrev = false;
        this.data.push(...timelines);
      });
  }

}

import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {TimeLineSnippet} from "../../dashboard-model";
import {HistoryKanbanService} from "../history-kanban/history-kanban.service";
import {HistoryAllService} from "./history-all.service";

@Component({
  selector: 'dashboard-history-all',
  styleUrls: ['./history-all.component.scss'],
  templateUrl: './history-all.component.html',
})
export class HistoryAllComponent implements OnInit, OnDestroy {
  private sseStream: Subscription;
  data: TimeLineSnippet[]  = [];
  page: number = 0;
  size: number = 10;
  loadedPrev: boolean = false;

  constructor(private historyAllService: HistoryAllService) {

  }

  ngOnInit(): void {
    this.sseStream = this.historyAllService.registerAllEvent(this.data);
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
    this.historyAllService.getPersistentTimelineData(this.page, this.size)
      .subscribe((timelines: TimeLineSnippet[]) =>  {
        if(timelines && timelines.length > 0) this.loadedPrev = true;
        else this.loadedPrev = false;
        this.data.push(...timelines);
      });
  }

}

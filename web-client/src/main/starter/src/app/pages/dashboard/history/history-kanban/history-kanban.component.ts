import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {History, TimeLineSnippet} from "../../dashboard-model";
import {HistoryKanbanService} from "./history-kanban.service";

@Component({
  selector: 'dashboard-history-kanban',
  styleUrls: ['./history-kanban.component.scss'],
  templateUrl: './history-kanban.component.html',
})
export class HistoryKanbanComponent implements OnInit, OnDestroy {
  private sseStream: Subscription;
  data: TimeLineSnippet[]  = [];
  page: number = 0;
  size: number = 10;
  loadedPrev: boolean = false;

  constructor(private historyKanbanService: HistoryKanbanService) {

  }

  ngOnInit(): void {
    this.sseStream = this.historyKanbanService.registerKanbanEvent(this.data);
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
    this.historyKanbanService.getPersistentTimelineData(this.page, this.size)
      .subscribe((timelines: TimeLineSnippet[]) =>  {
        if(timelines && timelines.length > 0) this.loadedPrev = true;
        else this.loadedPrev = false;
        this.data.push(...timelines);
      });
  }

}

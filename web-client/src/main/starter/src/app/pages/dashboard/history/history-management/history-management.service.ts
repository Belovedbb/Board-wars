import {Injectable} from '@angular/core';
import {Config} from "../../../../config/config";
import {ApiHttpService} from "../../../../@core/utils";
import {Observable, Subscription} from "rxjs";
import {map} from "rxjs/operators";
import {Messages, MessageService} from "../../../../@core/utils/message.service";
import {History, TimeLineSnippet, TimeLineSnippetBody, TimelineType} from "../../dashboard-model";

declare var EventSource;

@Injectable({ providedIn: 'root' })
export class HistoryManagementService {
  constructor(private messageService: MessageService, private http: ApiHttpService) { }

  registerManagementEvent(data: Array<TimeLineSnippet>) : Subscription {
    return this.observeMessages(Config.API_ROUTE.HISTORY_EVENT_MANAGEMENT)
      .subscribe((message: string) => {
        data.unshift(this.transformHistoryToTimeLineSnippet(JSON.parse(message)));
      });
  }

  private observeMessages(sseUrl: string): Observable<string> {
    return new Observable<string>(obs => {
      const es = new EventSource(sseUrl, { withCredentials: true });
      es.addEventListener('message', (evt) => {
        console.log(evt.data);
        obs.next(evt.data);
      });
      return () => es.close();
    });
  }

  private  transformHistoryToTimeLineSnippet(history: History): TimeLineSnippet {
    let timeline: TimeLineSnippet = new TimeLineSnippet();
    timeline.datetime = new Date(history.eventPeriod);
    timeline.header = history.title;
    timeline.user = history.username;
    timeline.type = this.transformCategoryToType(history.category);
    timeline.footer = 'Management Event';
    let body: TimeLineSnippetBody = new TimeLineSnippetBody();
    body.tag = 'h4';
    body.content = history.message;
    timeline.body = [body];
    return timeline;
  }

  getPersistentTimelineData(page: number, size: number): Observable<TimeLineSnippet[]> {
    return this.http.get(Config.resolveParameterLink(Config.API_ROUTE.HISTORY_PERSISTENT_MANAGEMENT,  ""+page, ""+size) )
      .pipe(
        map((response: History[]) => {
          if(!response){
            this.messageService.errorToast("Unknown Error", Messages.generic_error);
            return [];
          }
          return response.map(history => this.transformHistoryToTimeLineSnippet(history));
        })
      );
  }

  transformCategoryToType(category: string): TimelineType {
    if(category == 'SUCCESS') return TimelineType.CREATE;
    else if (category == 'WARNING') return TimelineType.UPDATE;
    else if (category == 'DANGER') return TimelineType.DELETE;
    else if(category == 'MILD') return TimelineType.LOG;
    else return TimelineType.DELETE;
  }



}

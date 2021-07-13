import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {Observable} from "rxjs";
import {Column, Task, TaskComment, TaskCommentResponse, TaskResponse} from "../../kanban-model";
import {HateoasUtil} from "../../../../@core/utils/hateoas.service";

@Injectable({ providedIn: 'root' })
export class TaskCommentService extends RestService<TaskCommentResponse> {

  constructor(injector: Injector) {
    super(TaskCommentResponse, 'kanban/task/task-comment', injector);
  }


  loadTaskCommentToDomain(data: any, isEmbedded: boolean = false) : TaskComment {
    if(!data) return null;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    data.timeCreated = new Date(data.timeCreated);
    return data;
  }

  loadTaskCommentArrayToDomain(data: any, isEmbedded: boolean = false) : TaskComment[] {
    let taskComments: TaskComment[] = [];
    if(!data) return taskComments;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    let i = 0;

    for(let taskComment of data) {
      if(i - 1 < 0) data[0].reply = false;
      else if(data[i - 1].teamUser.identity === data[i].teamUser.identity){
        taskComment.reply = false;
      }else{
        taskComment.reply = true;
      }
      taskComments.push(this.loadTaskCommentToDomain(taskComment, false));
      ++i;
    }
    return taskComments;
  }

}

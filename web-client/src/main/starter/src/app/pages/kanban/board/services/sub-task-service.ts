import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {Observable} from "rxjs";
import {
  Column,
  SubTask,
  SubTaskResponse,
  Task,
  TaskComment,
  TaskCommentResponse,
  TaskResponse
} from "../../kanban-model";
import {HateoasUtil} from "../../../../@core/utils/hateoas.service";

@Injectable({ providedIn: 'root' })
export class SubTaskService extends RestService<SubTaskResponse> {

  constructor(injector: Injector) {
    super(SubTaskResponse, 'kanban/task/sub-task', injector);
  }


  loadSubTaskToDomain(data: any, isEmbedded: boolean = false) : SubTask{
    if(!data) return null;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    data.startDate = new Date(data.startDate);
    data.endDate = new Date(data.endDate);
    return data;
  }

  loadSubTaskArrayToDomain(data: any, isEmbedded: boolean = false) : SubTask[] {
    let subTasks: SubTask[] = [];
    if(!data) return subTasks;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    for(let task of data) {
      subTasks.push(this.loadSubTaskToDomain(task, false));
    }
    return subTasks;
  }

}

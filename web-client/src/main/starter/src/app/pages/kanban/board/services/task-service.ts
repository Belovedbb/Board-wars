import {Injectable, Injector} from "@angular/core";
import {ResourceHelper, RestService} from "@lagoshny/ngx-hal-client";
import {Observable} from "rxjs";
import {Column, Task, TaskResponse} from "../../kanban-model";
import {HateoasUtil} from "../../../../@core/utils/hateoas.service";
import {TaskCommentService} from "./task-comment-service";
import {SubTaskService} from "./sub-task-service";

@Injectable({ providedIn: 'root' })
export class TaskService extends RestService<TaskResponse> {

  constructor(injector: Injector , private taskCommentService: TaskCommentService, private subTaskService: SubTaskService) {
    super(TaskResponse, 'kanban/task', injector);
  }


  getAllUsers() : Observable<Task[]> {
    return null
  }

  getFilteredUser(task: Task): Observable<Task> {
    return null;
  }

  static getTaskUrl(projectCode: number, columnName: string, position: number = null) : string {
    let pos = position !== null ? '/'+position : '';
    return ResourceHelper.getURL().concat('kanban/project/'+ projectCode +'/column/' + columnName + '/task' + pos);
  }

  loadTaskToDomain(data: any, isEmbedded: boolean = false) : Task {
    if(!data) return null;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    data.startDate = new Date(data.startDate);
    data.endDate = new Date(data.endDate);
    data.createdDate = new Date(data.createdDate);
    data.updatedDate = new Date(data.updatedDate);
    let taskCommentsData: any = data.comments;
    let subTasksData: any = data.subTasks;
    let newData : Task = <Task>data;
    newData.comments = this.taskCommentService.loadTaskCommentArrayToDomain(taskCommentsData, true);
    newData.subTasks = this.subTaskService.loadSubTaskArrayToDomain(subTasksData, true);
    return data;
  }

  loadTaskArrayToDomain(data: any, isEmbedded: boolean = false) : Task[] {
    let tasks: Task[] = [];
    if(!data) return tasks;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    for(let task of data) {
      tasks.push(this.loadTaskToDomain(task, false));
    }
    return tasks;
  }

}

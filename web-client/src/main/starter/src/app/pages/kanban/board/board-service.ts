import {Injectable} from "@angular/core";
import {ProjectService} from "./services/project-service";
import {TaskService} from "./services/task-service";
import {Column, Project, ProjectResponse, Task, TaskResponse, Transfer} from "../kanban-model";
import {catchError, filter, map} from "rxjs/operators";
import {Observable, of} from "rxjs";
import {GlobalService} from "../../../@core/utils/global.service";
import {ApiHttpService} from "../../../@core/utils";
import {CdkDragDrop} from "@angular/cdk/drag-drop";
import {MessageService} from "../../../@core/utils/message.service";

@Injectable({ providedIn: 'root' })
export class BoardService  {

  constructor(private http: ApiHttpService, private projectService: ProjectService, private messageService: MessageService,
              private taskService: TaskService, private globalService: GlobalService) {

  }

  loadBoardProject(): Observable<Project[]> {
    return this.projectService.getAllProjects();
  }

  getProject(code: number) : Observable<Project> {
    return this.projectService.get(code).pipe(
      filter((projectResponse) => projectResponse.success === true),
      map((projectResponse: ProjectResponse) => this.projectService.loadProjectToDomain(projectResponse.body)));
  }

  formatDate(date: Date): string {
    return this.globalService.formatDate(date);
  }

  moveTask(event: CdkDragDrop<Task[]>, columnLimit: number, project: Project, column: Column,
           prevColumnName: string, transfer: Transfer) : Observable<boolean> {
    let url: string = '';
    if(transfer === Transfer.IN) {
      url = TaskService.getTaskUrl(project.code, column.name, event.previousIndex) + '/move-to/column/'+ column.name+'/task/'+event.currentIndex;
    }else {
      url = TaskService.getTaskUrl(project.code, prevColumnName, event.previousIndex) + '/move-to/column/'+ column.name +'/task/'+event.currentIndex;
    }

    return this.http.get(url)
      .pipe(
        map((response : TaskResponse) =>  {
          return true;
        }),
        catchError((err, caught) => {
          return of(false);
        })
      );
  }

  showRevertMessage() {
    this.messageService.errorToast("Changes not persisted".toUpperCase(), "Reverted");
  }

}

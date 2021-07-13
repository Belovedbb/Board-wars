import {Injectable, Injector} from "@angular/core";
import {Observable} from "rxjs";
import {ApiHttpService} from "../../../../../@core/utils";
import {filter, map} from "rxjs/operators";
import {UserService} from "../../../../management/user/user-management-service";
import {Messages, MessageService} from "../../../../../@core/utils/message.service";
import {Router} from "@angular/router";
import {BoardService} from "../../../../kanban/board/board-service";
import {ColumnForm, ColumnResponse, Project, ProjectForm, ProjectResponse} from "../../../../kanban/kanban-model";
import {ProjectService} from "../../../../kanban/board/services/project-service";
import {KanbanSettingsComponent} from "./kanban-settings.component";


@Injectable()
export class KanbanSettingsService {

  constructor(private http: ApiHttpService, private boardService: BoardService, private projectService: ProjectService,
              private userService: UserService, private messageService: MessageService,private router: Router) {}


  getAllProjects(): Observable<Project[]> {
    return this.projectService.getAllProjects()
  }

  getProject(code: number, kanbanSettings: KanbanSettingsComponent): Observable<Project> {
    return this.projectService.get(code).pipe(
      filter((projectResponse) => projectResponse.success === true),
      map((projectResponse: ProjectResponse) => {
        kanbanSettings.selectedProjectLink = projectResponse.getSelfLinkHref();
        return this.projectService.loadProjectToDomain(projectResponse.body)
      }));
  }

  submitActiveUserForm(parentRef: KanbanSettingsComponent) : void {
    if(parentRef.formGroup.valid){
      let projectForm = new ProjectForm();
      let formValue = parentRef.formGroup.value;
      projectForm.name = formValue.projectName;
      projectForm.description = formValue.projectDescription;


      let columnForm = null;
      if(parentRef.selectedColumn) {
        columnForm = new ColumnForm();
        columnForm.color = formValue.columnColor;
        columnForm.taskLimit = formValue.columnTaskLimit;
        columnForm.description = formValue.columnDescription;
      }

      this.handleProjectPatch(parentRef, projectForm, columnForm);
    }
  }

  handleProjectPatch(parentRef: KanbanSettingsComponent, projectForm: ProjectForm, columnForm: ColumnForm): void {
    this.http.patch(parentRef.selectedProjectLink, projectForm)
      .pipe(
        map((response: ProjectResponse) => {
          if(!response.success){
            this.handleErrorToast();
          }else{
            if(columnForm) {
              this.handleColumnPatch(parentRef, columnForm)
            }else {
              this.reloadComponent();
            }
          }
        })
      )
      .subscribe();
  }

  handleColumnPatch(parentRef: KanbanSettingsComponent, columnForm: ColumnForm): void {
    this.http.patch(parentRef.selectedColumnLink, columnForm)
      .pipe(
        map((response: ColumnResponse) => {
          if(!response.success){
            this.handleErrorToast();
          }else{
            this.reloadComponent();
          }
        })
      )
      .subscribe();
  }

  handleErrorToast(): void {
    this.messageService.errorToast('Error', Messages.generic_error);
  }

  reloadComponent() {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigateByUrl('/pages/settings/general');
  }


}

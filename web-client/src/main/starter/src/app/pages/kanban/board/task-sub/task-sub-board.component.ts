import {Component, Input, OnInit} from '@angular/core';
import {NbDialogRef, NbTagComponent} from '@nebular/theme';
import {ControlContainer, FormBuilder, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Observable, of} from "rxjs";
import {GlobalService} from "../../../../@core/utils/global.service";
import {
  CategoryPart,
  CategoryType,
  Column,
  OPERATION,
  Project, Task, TaskCommentForm, TaskCommentResponse,
  TaskForm, TaskResponse, TeamUser
} from "../../kanban-model";
import {Team, User} from "../../../management/management-model";
import {TeamService} from "../../../management/team/team-management-service";
import {flatMap, map} from "rxjs/operators";
import {ResourceHelper} from "@lagoshny/ngx-hal-client";
import {ApiHttpService} from "../../../../@core/utils";
import {Messages, MessageService} from "../../../../@core/utils/message.service";
import {KanbanBoardComponent} from "../board.component";
import {ProjectService} from "../services/project-service";
import {UserService} from "../../../management/user/user-management-service";
import {TaskCommentService} from "../services/task-comment-service";
import {TaskService} from "../services/task-service";

@Component({
  selector: 'task-sub-board',
  templateUrl: 'task-sub-board.component.html',
  styleUrls: ['task-sub-board.component.scss'],
  viewProviders:[{provide:ControlContainer,useExisting: FormGroupDirective }],
  providers: [ FormBuilder]
})

export class TaskSubBoardComponent implements OnInit {

  @Input() title: string;
  @Input() type: OPERATION;
  @Input() parent: KanbanBoardComponent;
  @Input() task: Task;
  @Input() column: Column = null;
  @Input() project: Observable<Project> = null;

  taskTags: string[] = [];
  taskInputTag :string = null;
  user: Observable<User> = null;
  users: Observable<User[]> = null;
  initialDate: Date = new Date();
  categories: CategoryType[] = [];
  assignees: Observable<User[]> = null;
  selectedProject: Project = null;

  formGroup: FormGroup;

  description: string;

  constructor(private globalService: GlobalService, private teamService: TeamService, private userService: UserService,
              private http: ApiHttpService, private projectService: ProjectService, private messageService: MessageService,
              private taskCommentService: TaskCommentService,
              protected ref: NbDialogRef<TaskSubBoardComponent>,  private formBuilder: FormBuilder) {
    this.categories = TaskSubBoardComponent.buildCategories();
    this.users = this.userService.getAllUsers();
    this.user = this.globalService.getLoggedInUser();
    this.assignees = this.user.pipe(map((user: User) => [user]));
    this.formGroup = this.formBuilder.group({
      inputTitle0: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      inputStartDate0: [new Date(), Validators.required],
      inputEndDate0: [new Date(), Validators.required],
      inputDescription0: [''],
      inputTag0: [this.taskTags],
      inputAssignee0: ['', Validators.required],
      inputCategory0: [this.categories[0].part, Validators.required]
    });
  }

  ngOnInit() {
    this.initializer();
  }

  static buildCategories() : CategoryType[] {
    let categories = [];
    categories.push(new CategoryType(CategoryPart.LOW, "#1b54ee"));
    categories.push(new CategoryType(CategoryPart.SUBTLE, "#10eee5"));
    categories.push(new CategoryType(CategoryPart.AVERAGE, "#8aee1f"));
    categories.push(new CategoryType(CategoryPart.HIGH, "#ee788c"));
    categories.push(new CategoryType(CategoryPart.DANGER, "#ee1829"));
    return categories;
  }

  initializer(): void{
    this.project.pipe(
      map(proj => {
        this.selectedProject = proj;
        if(proj && proj.teamUser && proj.teamUser.type === 'TEAM') {
          this.assignees =  this.getTeamAssignee(proj);
        }
        this.globalService.getLoggedInUserName().subscribe(name => {
          this.formGroup.get('inputAssignee0').setValue(name);
        })

      })
    ).subscribe();
  }

  getTeamAssignee(proj: Project): Observable<User[]> {
    let team  = new Team();
    team.code = proj.teamUser.identity;
    return this.teamService.getFilteredTeam(team)
      .pipe(
        flatMap((team: Team) => {
          if(team.members && team.members.length > 0) {
            return of(team.members);
          }
        })
      );
  }

  dismiss() {
    this.ref.close();
  }

  onAddTaskTag(ref): void {
    if(ref && ref.value){
      if(!this.taskTags.includes(ref.value)) {
        this.taskTags.push(ref.value);
      }
      ref.value = '';
    }
  }

  onTaskProjectTag(ele: NbTagComponent): void {
    let value = ele.text;
    if(value) {
      const index = this.taskTags.indexOf(value);
      if (index > -1) {
        this.taskTags.splice(index, 1);
      }
    }
  }

  onNewTaskSubmit() {
    if(this.formGroup.valid){
      this.loadNewTaskForm().pipe(
        flatMap(form => this.project.pipe(flatMap(proj => this.http.post(TaskService.getTaskUrl(proj.code, this.column.name), form)))),
        map((response: TaskResponse) => {
          if(!response.success){
            this.handleErrorToast();
          }else {
            this.parent.ngOnInit();
            this.dismiss();
          }
          return response;
        })
      ).subscribe();
    }
  }

  loadNewTaskForm(): Observable<TaskForm> {
    let taskForm = new TaskForm();
    let formValue = this.formGroup.value;
    taskForm.name = formValue.inputTitle0;
    taskForm.description = tinymce.activeEditor.getContent();
    taskForm.startDate = formValue.inputStartDate0;
    taskForm.endDate = formValue.inputEndDate0;
    taskForm.categories = [formValue.inputCategory0];
    taskForm.tags = this.taskTags;
    let assignee = new TeamUser();
    assignee.type = "USER";
    assignee.identity = formValue.inputAssignee0;
    taskForm.assignee = assignee;
    console.log(TaskForm);
    return of(taskForm);
  }

  onNewTaskCommentSubmit(task: Task, project: Observable<Project>, column: Column, ref): void {
    if(ref && ref.value) {
      let value = ref.value;
      this.globalService.getLoggedInUserName().pipe(
        flatMap(username => this.project.pipe(flatMap(proj => this.http.post(ResourceHelper.getURL()
          .concat('kanban/project/'+ proj.code +'/column/' + this.column.name + '/task/' +task.position+'/task-comment'),
          this.loadNewTaskCommentForm(value, username))))),
        map((response: TaskCommentResponse) => {
          if(!response.success){
            this.handleErrorToast();
          }else {
            this.ngOnInit();
            if(!task.comments) task.comments = [];
            task.comments.push(this.taskCommentService.loadTaskCommentToDomain(response.body));
          }
          ref.value = '';
          return response;
        })
      ).subscribe();
    }
  }

  loadNewTaskCommentForm(value: string, username: string): TaskCommentForm {
    let taskCommentForm = new TaskCommentForm();
    taskCommentForm.timeCreated= new Date();
    taskCommentForm.comment = value;
    let teamUser = new TeamUser();
    teamUser.type = 'USER';
    teamUser.identity = username;
    taskCommentForm.teamUser = teamUser;
    console.log(taskCommentForm);
    return taskCommentForm;
  }


  getCategoryIcon(task: Task) : string {
    if(task) {
      switch(task.categories[0]){
        case CategoryPart.LOW: return "task-body-blue-down-double";
        case CategoryPart.SUBTLE: return "task-body-blue-down";
        case CategoryPart.AVERAGE: return "task-body-green-dough";
        case CategoryPart.HIGH: return "task-body-red-up";
        case CategoryPart.DANGER: return "task-body-red-up-double";
      }
    }
    return "";
  }

  onEditTaskSubmit() {
    if(this.formGroup.valid){

    }
  }

  handleErrorToast(): void {
    this.messageService.errorToast('Error', Messages.generic_error);
  }

  formatDate(date: Date): string {
    return this.globalService.formatDate(date);
  }

}

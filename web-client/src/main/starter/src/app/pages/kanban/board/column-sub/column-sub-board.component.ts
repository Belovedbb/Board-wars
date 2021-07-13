import {Component, ElementRef, Input, OnInit, TemplateRef} from '@angular/core';
import {
  ControlContainer,
  FormBuilder,
  FormGroup,
  FormGroupDirective, Validators
} from '@angular/forms';
import {Observable, of} from "rxjs";
import {GlobalService} from "../../../../@core/utils/global.service";
import {ColumnForm, ColumnResponse, Project, ProjectForm, ProjectResponse, TeamUser} from "../../kanban-model";
import {TeamService} from "../../../management/team/team-management-service";
import {ApiHttpService} from "../../../../@core/utils";
import {Messages, MessageService} from "../../../../@core/utils/message.service";
import {KanbanBoardComponent} from "../board.component";
import {ProjectService} from "../services/project-service";
import {User} from "../../../management/management-model";
import {ResourceHelper} from "@lagoshny/ngx-hal-client";
import {map} from "rxjs/operators";
import {flatMap} from "rxjs/internal/operators";
import {Router} from "@angular/router";

@Component({
  selector: 'column-sub-board',
  templateUrl: 'column-sub-board.component.html',
  styleUrls: ['column-sub-board.component.scss'],
  viewProviders:[{provide:ControlContainer,useExisting: FormGroupDirective }],
  providers: [ FormBuilder]
})

export class ColumnSubBoardComponent implements OnInit {

  @Input() title: string;
  @Input() type: string;
  @Input() project: Observable<Project> = null;
  @Input() parent: KanbanBoardComponent;

  formGroup: FormGroup;

  description: string;

  constructor(private globalService: GlobalService, private teamService: TeamService,
              private http: ApiHttpService, private projectService: ProjectService, private messageService: MessageService,
              private formBuilder: FormBuilder, private router: Router) {

    this.formGroup = this.formBuilder.group({
      columnName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      color: [''],
      description: [''],
      limit: [4, Validators.required]
    });
  }

  ngOnInit() {
    this.initializer();
  }

  initializer(): void{

  }


  onNewColumnSubmit() {
    if(this.formGroup.valid && this.project){
      of(this.loadNewColumnForm())
        .pipe(
          flatMap(form => this.project.pipe(flatMap(proj => this.http.post(ResourceHelper.getURL()
            .concat('kanban/project/'+ proj.code +'/column'), form)))),
          map((response: ColumnResponse) => {
            if(!response.success){
              this.handleErrorToast();
            }else {
              this.reloadComponent();
            }
            return response;
          })
        ).subscribe();
    }
  }

  loadNewColumnForm(): ColumnForm {
    let columnForm = new ColumnForm();
    let formValue = this.formGroup.value;
    columnForm.description = formValue.description;
    columnForm.name = formValue.columnName;
    columnForm.color = formValue.color;
    columnForm.taskLimit = formValue.limit;
    console.log(columnForm);
    return columnForm;
  }

  handleErrorToast(): void {
    this.messageService.errorToast('Error', Messages.generic_error);
  }

  reloadComponent() {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigateByUrl('/pages/kanban/board');
  }


}

import {Component, Input, OnInit} from '@angular/core';
import {NbDialogRef, NbTagComponent} from '@nebular/theme';
import {
  ControlContainer,
  FormBuilder,
  FormGroup,
  FormGroupDirective, Validators
} from '@angular/forms';
import {Observable} from "rxjs";
import {GlobalService} from "../../../../@core/utils/global.service";
import {Project, ProjectForm, ProjectResponse, TeamUser} from "../../kanban-model";
import {Team, User} from "../../../management/management-model";
import {TeamService} from "../../../management/team/team-management-service";
import {HateoasUtil} from "../../../../@core/utils/hateoas.service";
import {filter, flatMap, map} from "rxjs/operators";
import {ResourceHelper} from "@lagoshny/ngx-hal-client";
import {ApiHttpService} from "../../../../@core/utils";
import {Messages, MessageService} from "../../../../@core/utils/message.service";
import {KanbanBoardComponent} from "../board.component";
import {ProjectService} from "../services/project-service";

@Component({
  selector: 'sub-board',
  templateUrl: 'sub-board.component.html',
  styleUrls: ['sub-board.component.scss'],
  viewProviders:[{provide:ControlContainer,useExisting: FormGroupDirective }],
  providers: [ FormBuilder]
})

export class SubBoardComponent implements OnInit {

  @Input() title: string;
  @Input() type: string;
  @Input() parent: KanbanBoardComponent;

  projectTags: string[] = [];
  projectInputTag :string = null;
  user: Observable<User> = null;
  teams: Observable<Team[]> = null;
  initialDate: Date = new Date();

  formGroup: FormGroup;

  description: string;

  constructor(private globalService: GlobalService, private teamService: TeamService,
              private http: ApiHttpService, private projectService: ProjectService, private messageService: MessageService,
              protected ref: NbDialogRef<SubBoardComponent>,  private formBuilder: FormBuilder) {
    this.user = this.globalService.getLoggedInUser();

    this.formGroup = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      endDate: [new Date(), Validators.required],
      description: [''],
      tags: [this.projectTags],
      status: ['Active', Validators.required],
      selectedTeam: ['']
    });
  }

  ngOnInit() {
    this.initializer();
  }


  initializer(): void{

  }

  dismiss() {
    this.ref.close();
  }

  onAddProjectTag(ref): void {
    if(ref && ref.value){
      if(!this.projectTags.includes(ref.value)) {
        this.projectTags.push(ref.value);
      }
      ref.value = '';
    }
  }

  onRemoveProjectTag(ele: NbTagComponent): void {
    let value = ele.text;
    if(value) {
      const index = this.projectTags.indexOf(value);
      if (index > -1) {
        this.projectTags.splice(index, 1);
      }
    }
  }

  onTeamUserSelect(val) {
    if (val !== 'Private') {
      this.teams = this.teamService.getAllUserTeams().pipe(map(team => {
        if(team && team.length > 0) {
          this.formGroup.get('selectedTeam').setValue(team[0].code);
        }
        return team;
      }));
    }else {
      this.formGroup.get('selectedTeam').setValue('');
      this.teams = null;
    }
  }


  onNewProjectSubmit() {
    if(this.formGroup.valid){
      this.user.pipe(
        map(user => this.loadNewProjectForm(user)),
        flatMap((projectForm) => this.http.post(ResourceHelper.getURL().concat('kanban/project'), projectForm)),
        map((response: ProjectResponse) => {
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

  loadNewProjectForm(user: User): ProjectForm {
    let projectForm = new ProjectForm();
    let formValue = this.formGroup.value;
    projectForm.status = formValue.status.toUpperCase();
    projectForm.description = formValue.description;
    projectForm.name = formValue.name;
    projectForm.endPeriod = formValue.endDate;
    projectForm.tags = this.projectTags;
    let teamUser: TeamUser = new TeamUser();
    if(formValue.selectedTeam) {
      teamUser.identity = formValue.selectedTeam;
      teamUser.type = 'TEAM';
    }else {
      teamUser.identity = user.username;
      teamUser.type = 'USER';
    }
    projectForm.teamUser = teamUser;
    console.log(projectForm);
    return projectForm;
  }

  handleErrorToast(): void {
    this.messageService.errorToast('Error', Messages.generic_error);
  }

}

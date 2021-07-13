import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {Team, TeamForm, TeamResponse, User, UserForm, UserResponse} from "../../../management-model";
import {filter, map, mergeAll, toArray} from "rxjs/operators";
import {HateoasUtil} from "../../../../../@core/utils/hateoas.service";
import {UserService} from "../../../user/user-management-service";
import {ActiveUserComponent} from "../../../user/profile/active-user/active-user.component";
import {SelfTeamComponent} from "./self-team.component";
import {ApiHttpService} from "../../../../../@core/utils";
import {Messages, MessageService} from "../../../../../@core/utils/message.service";
import {Router} from "@angular/router";


@Injectable()
export class SelfTeamService {

  constructor(private http: ApiHttpService, private userService: UserService, private messageService: MessageService,private router: Router) {}

  getAllUserTeams(team: Team) : Observable<User[]> {
    return this.userService.getAllUsers()
      .pipe(
        mergeAll(),
        filter((user: User) => !(this.getTeamMemberList(team).includes(user.username))),
        toArray(),
        map((array: User[]) => {
          array.push(...team.members);
          return array;
        })
      );
  }

  private getTeamMemberList(team: Team): string[] {
    let list: string[] = [];
    if(team && team.members && team.members.length > 0){
      team.members.forEach(user => {
        list.push(user.username);
      })
    }
    return list;
  }

  submitSelfTeamForm(parentRef: SelfTeamComponent) : void {
    if(parentRef.formGroup.valid){
      let teamForm = new TeamForm();
      let formValue = parentRef.formGroup.value;
      teamForm.active = formValue.status === parentRef.statuses[0].key;
      teamForm.description = formValue.description;
      teamForm.name = formValue.name;
      teamForm.colorCode = formValue.colorCode;
      teamForm.members = this.transformUserNamesToTeamMembers(formValue.teamMembers, parentRef.team.leader.username);
      console.log(teamForm);
      this.http.patch(parentRef.team.parent.getSelfLinkHref(), teamForm)
        .pipe(
          map((response: TeamResponse) => {
            if(!response.success){
              this.handleErrorToast();
            }else{
              this.reloadComponent();
            }
          })
        )
        .subscribe();
    }
  }

  handleErrorToast(): void {
    this.messageService.errorToast('Error', Messages.generic_error);
  }

  reloadComponent() {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigateByUrl('/pages/management/team');
  }

  transformUserNamesToTeamMembers(names: string[], ...include: string[]): User[] {
    let users: User[] = [];
    if(names && names.length > 0){
      this.appendStringToUser(names, users);
    }
    if(include && include.length > 0){
      this.appendStringToUser(include, users);
    }
    return [...new Map(users.map(object => {
      return [object["username"], object];
    })).values()];
  }

  appendStringToUser(values: string[], list: User[]): void {
    values.forEach(value => {
      let user: User = new User();
      user.username = value;
      list.push(user);
    });
  }

}

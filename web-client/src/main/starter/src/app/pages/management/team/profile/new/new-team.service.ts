import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {Team, TeamForm, TeamResponse, User, UserForm, UserResponse} from "../../../management-model";
import {filter, map, mergeAll, toArray} from "rxjs/operators";
import {UserService} from "../../../user/user-management-service";
import {ApiHttpService} from "../../../../../@core/utils";
import {Messages, MessageService} from "../../../../../@core/utils/message.service";
import {Router} from "@angular/router";
import {NewTeamComponent} from "./new-team.component";
import {GlobalService} from "../../../../../@core/utils/global.service";
import {TeamService} from "../../team-management-service";
import {ResourceHelper} from "@lagoshny/ngx-hal-client";


@Injectable()
export class NewTeamService {

  constructor(private http: ApiHttpService, private userService: UserService,
              private globalService: GlobalService, private messageService: MessageService,
              private router: Router) {}

  getAllTeamMembers() : Observable<User[]> {
    return this.userService.getAllUsers()
      .pipe(
        mergeAll(),
        toArray()
      );
  }

  getLoggedInUser(): Observable<User> {
    return this.globalService.getLoggedInUser();
  }

  submitSelfTeamForm(parentRef: NewTeamComponent) : void {
    if(parentRef.formGroup.valid){
      let teamForm = new TeamForm();
      let formValue = parentRef.formGroup.value;
      teamForm.active = formValue.status === parentRef.statuses[0].key;
      teamForm.description = formValue.description;
      teamForm.name = formValue.name;
      teamForm.colorCode = formValue.color;
      let transformedLeader = this.transformUserNamesToTeamMembers([parentRef.team.leader.username], null);
      teamForm.leader = transformedLeader && transformedLeader.length > 0 ? transformedLeader[0] : null;
      teamForm.members = this.transformUserNamesToTeamMembers(formValue.teamMembers, parentRef.team.leader.username);
      console.log(teamForm);
      this.http.post(ResourceHelper.getURL().concat('management/team'), teamForm)
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
    return [...new Map(users.map(object =>[object["username"], object])).values()];
  }

  appendStringToUser(values: string[], list: User[]): void {
    values.forEach(value => {
      let user: User = new User();
      user.username = value;
      list.push(user);
    });
  }

  generateColor(): string {
    let numOfSteps = Math.floor(Math.random() * 30) , step = 12;
    let r, g, b;
    let h = step / numOfSteps;
    let i = ~~(h * 6);
    let f = h * 6 - i;
    let q = 1 - f;
    switch(i % 6){
      case 0: r = 1; g = f; b = 0; break;
      case 1: r = q; g = 1; b = 0; break;
      case 2: r = 0; g = 1; b = f; break;
      case 3: r = 0; g = q; b = 1; break;
      case 4: r = f; g = 0; b = 1; break;
      case 5: r = 1; g = 0; b = q; break;
    }
    let c = "#" + ("00" + (~ ~(r * 255)).toString(16)).slice(-2) + ("00" + (~ ~(g * 255))
      .toString(16)).slice(-2) + ("00" + (~ ~(b * 255)).toString(16)).slice(-2);
    return (c);
  }

}

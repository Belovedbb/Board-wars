import { Component, OnInit, Input } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {PictureSnippet, Team, User} from "../../../management-model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs";
import {UserService} from "../../../user/user-management-service";
import {NewTeamService} from "./new-team.service";
import {GlobalService} from "../../../../../@core/utils/global.service";

@Component({
  selector: 'profile-new-team',
  templateUrl: './new-team.component.html',
  styleUrls: ['./new-team.component.scss'],
  providers: [NewTeamService, UserService, GlobalService]
})
export class NewTeamComponent implements OnInit {

  formGroup: FormGroup;
  team: Team = new Team();
  teamMembers: User[] = [];
  allSelectableUsers: Observable<User[]> = null;

  statuses = [{key: 'active', value: 'Active'}, {key: 'inactive', value: 'Inactive'}];

  constructor(private newTeamService: NewTeamService, private formBuilder: FormBuilder) {
    this.formGroup = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      leader: [{value: '', disabled: true}],
      color: [''],
      description: [''],
      status: ['', Validators.required],
      teamMembers: [[], Validators.required]
    });
  }

  //TODO add filter for inactive user
  ngOnInit() {
    this.newTeamService.getLoggedInUser().subscribe(user => this.initializer(user));
  }

  initializer(loggedInUser: User): void{
    this.formGroup.get('status').setValue(this.statuses[0].key);
    this.formGroup.get('leader').setValue(loggedInUser.username);
    this.formGroup.get('teamMembers').setValue([loggedInUser.username])
    this.teamMembers = [loggedInUser];
    this.team.leader = loggedInUser;
    this.formGroup.get('color').setValue(this.newTeamService.generateColor());
    this.allSelectableUsers = this.newTeamService.getAllTeamMembers();
  }

  onSubmit() {
    this.newTeamService.submitSelfTeamForm(this);
  }

  compareWith(first: any, second: any): boolean {
    let x = (typeof first == 'string' || first instanceof String) ? first : first.username;
    let y = (typeof second == 'string' || second instanceof String) ? second : second.username;
    return x === y;
  }
}

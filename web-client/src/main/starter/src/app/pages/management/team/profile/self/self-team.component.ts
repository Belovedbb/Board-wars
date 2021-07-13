import { Component, OnInit, Input } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {PictureSnippet, Team, User} from "../../../management-model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SelfTeamService} from "./self-team.service";
import {Observable} from "rxjs";
import {UserService} from "../../../user/user-management-service";

@Component({
  selector: 'profile-self-team',
  templateUrl: './self-team.component.html',
  styleUrls: ['./self-team.component.scss'],
  providers: [SelfTeamService, UserService]
})
export class SelfTeamComponent implements OnInit {

  firstName: string;
  lastName: string;
  formGroup: FormGroup;
  @Input() team: Team;

  constructor(private selfTeamService: SelfTeamService, private formBuilder: FormBuilder) {

  }

  teamMembers: User[] = [];
  allSelectableUsers: Observable<User[]> = null;

  statuses = [{key: 'active', value: 'Active'}, {key: 'inactive', value: 'Inactive'}];

  ngOnInit() {
    this.formGroup = this.formBuilder.group({
      name: [this.team.name, [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      code: [this.team.code],
      leader: [this.team.leader.firstName],
      color: [this.team.colorCode],
      description: [this.team.description],
      status: ['', Validators.required],
      teamMembers: [this.team.members ? [...this.team.members.map(user => user.username)] : [], Validators.required]
    });

    let key = this.statuses[0].key;
    if(!this.team.active) {
      key = this.statuses[1].key;
    }

    this.formGroup.get('status').setValue(key);
    this.teamMembers = this.team.members;
    this.allSelectableUsers = this.selfTeamService.getAllUserTeams(this.team);
  }


  onSubmit() {
    this.selfTeamService.submitSelfTeamForm(this);
  }

  compareWith(first: any, second: any): boolean {
    let x = (typeof first == 'string' || first instanceof String) ? first : first.username;
    let y = (typeof second == 'string' || second instanceof String) ? second : second.username;
    return x === y;
  }
}

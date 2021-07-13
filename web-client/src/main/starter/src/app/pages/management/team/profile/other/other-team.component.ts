import { Component, OnInit, Input } from '@angular/core';
import {Team, User} from "../../../management-model";
import {OtherTeamService} from "./other-team.service";

@Component({
  selector: 'profile-other-team',
  templateUrl: './other-team.component.html',
  styleUrls: ['./other-team.component.scss'],
  providers: [OtherTeamService]
})
export class OtherTeamComponent implements OnInit {

  @Input() team: Team;
  teamMembers: User[] = [];

  statuses = [{key: 'active', value: 'Active'}, {key: 'inactive', value: 'Inactive'}];

  constructor(private otherTeamService: OtherTeamService) {

  }

  ngOnInit() {
    this.teamMembers = this.team.members;
  }

  compareWith(first: any, second: any): boolean {
    let x = (typeof first == 'string' || first instanceof String) ? first : first.username;
    let y = (typeof second == 'string' || second instanceof String) ? second : second.username;
    return x === y;
  }
}

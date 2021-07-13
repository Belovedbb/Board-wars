import {Component, OnDestroy, OnInit} from '@angular/core';
import {SUB_MENU_ITEMS} from "../../pages-menu";
import {NbMenuItem} from "@nebular/theme";
import {Observable} from "rxjs";
import {Team, User} from "../management-model";
import {map} from "rxjs/operators";
import {TeamService} from "./team-management-service";
import {GlobalService} from "../../../@core/utils/global.service";
import {HateoasUtil} from "../../../@core/utils/hateoas.service";

const sidebarItems: NbMenuItem[] = [
  {
    title: 'User',
    icon: 'person-outline',
    link: '/pages/management/user'
  },
  {
    title: 'Team',
    icon: 'person-add-outline',
    link: '/pages/management/team',
    selected: true
  },
];

@Component({
  selector: 'team-management',
  styleUrls: ['./team-management.component.scss'],
  templateUrl: './team-management.component.html',
  providers: [TeamService, GlobalService, HateoasUtil]
})
export class TeamManagementComponent implements OnInit,  OnDestroy {

  teams: Observable<Team[]>;
  currentTeam: Team;

  constructor( private teamService: TeamService){
    this.initialize();
    this.teams = teamService.getAllUserTeams();
  }

  ngOnDestroy() {
  }

  ngOnInit(): void {
  }

  initialize(): void {
    SUB_MENU_ITEMS.length = 0;
    for (let item of sidebarItems){
      SUB_MENU_ITEMS.push(item);
    }
  }

  selectTeam(team: Team): void {
    this.teamService.getFilteredTeam(team)
      .pipe(map(team => {
        this.currentTeam = team;
      })).subscribe();
  }

}

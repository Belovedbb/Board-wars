import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {Team, TeamResponse, User} from "../management-model";
import {Observable} from "rxjs";
import {filter, map, mergeAll, toArray} from "rxjs/operators";
import {GlobalService} from "../../../@core/utils/global.service";
import {ApiHttpService} from "../../../@core/utils";
import {HateoasUtil} from "../../../@core/utils/hateoas.service";
import {flatMap} from "rxjs/internal/operators";

@Injectable({ providedIn: 'root' })
export class TeamService extends RestService<TeamResponse> {

  constructor(injector: Injector, private globalService: GlobalService, private http: ApiHttpService, private hateoasUtil: HateoasUtil<TeamResponse>) {
    super(TeamResponse, 'management/team', injector);
  }

  getAllUserTeams() : Observable<Team[]> {
    return this.globalService.getLoggedInUser()
      .pipe(
        flatMap((user) => this.hateoasUtil.getAllFollowUp(TeamResponse, 'management/team', ['user', user.username] )),
        mergeAll(),
        filter((response: TeamResponse) => response.success),
        map((response: TeamResponse) => {
          let team: Team = response.body;
          team.parent = response;
          return team;
        }),
        toArray()
        );
  }

  getFilteredTeam(team: Team): Observable<Team> {
    return this.get(team.code)
      .pipe(
        filter((response: TeamResponse) => response.success),
        flatMap((response: TeamResponse) => {
          let currentTeam = response.body;
          return this.globalService.getLoggedInUser().pipe(map((user:User) => {
            currentTeam.isLeader = this.isTeamLeader(user, currentTeam);
            currentTeam.parent = response;
            return currentTeam;
          }));
        })
      );
  }

  isTeamLeader(user: User, team: Team): boolean {
    return user && team && team.leader && user.username && team.leader.username && user.username === team.leader.username;
  }






}

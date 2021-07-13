import {Injectable, Injector} from "@angular/core";
import {Observable} from "rxjs";
import {ApiHttpService} from "../../../../../@core/utils";
import {User, UserForm, UserResponse} from "../../../management-model";
import {map} from "rxjs/operators";
import {OtherTeamComponent} from "./other-team.component";
import {TeamService} from "../../team-management-service";
import {Messages, MessageService} from "../../../../../@core/utils/message.service";
import {Router} from "@angular/router";


@Injectable()
export class OtherTeamService {

  constructor(private http: ApiHttpService, private teamService: TeamService, private messageService: MessageService,private router: Router) {}



}

import {Observable, of} from 'rxjs';
import { Injectable } from '@angular/core';
import {Team, User, UserResponse} from "../../pages/management/management-model";
import {ApiHttpService} from "./http.service";
import {catchError, filter, map} from "rxjs/operators";
import {Config} from "../../config/config";
import {Messages, MessageService} from "./message.service";
import {GlobalMarker} from "../../pages/marker/marker-model";

@Injectable({ providedIn: 'root' })
export class GlobalService  {

  private readonly user: Observable<User>;
  private name: Observable<string>;

  constructor(private http: ApiHttpService, private messageService: MessageService){
    this.user = this.http.get(Config.API_ROUTE.BASE + '/management/user/current')
      .pipe(
        filter((data: UserResponse ) => data.success),
        map((data: UserResponse) => data.body),
        catchError((err, caught) => {
          this.messageService.errorToast("Unknown Error", Messages.generic_error);
          return caught;
        }));
    this.name = this.user.pipe(map(user => user.username))
  }

  getLoggedInUser(): Observable<User> {
    return this.user;
  }

  getLoggedInUserName(): Observable<string> {
    return this.name;
  }

  isLoggedIn(): Observable<boolean> {
    return of(true);
  }

  formatDate(date: Date): string {
    if(!date) return "";
    let ye = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(date);
    let mo = new Intl.DateTimeFormat('en', { month: 'short' }).format(date);
    let da = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(date);
    return `${da}-${mo}-${ye}`;
  }

  getGlobalMarker(): Observable<GlobalMarker> {
    return this.http.get(Config.API_ROUTE.GLOBAL_MARKER)
      .pipe(
        map((data: GlobalMarker) => data),
        catchError((err, caught) => {
          this.messageService.errorToast("Unknown Error", Messages.generic_error);
          return caught;
        }))
  }


}

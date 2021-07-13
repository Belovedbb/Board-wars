import { HttpClient } from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {Config} from "../config/config";
import {catchError, map, timeout} from "rxjs/operators";
import {AuthGuard} from "./auth.guard";
import {DOCUMENT} from "@angular/common";

@Injectable({
  providedIn: 'root',
})

export class AuthService {

  constructor(private http: HttpClient, @Inject(DOCUMENT) private document: Document) { }

  isAuthenticated() : Observable<AuthenticationResponse> {
      return this.http.get<AuthenticationResponse>(Config.API_ROUTE.AUTH_STATE, {
        withCredentials: true
      })
        .pipe(
          timeout(Config.NETWORK.TIMEOUT),
          map(data =>{
          return data;
        }),
        catchError((err, caught) => {
          console.log(err.message);
          throw err;
        })
        )
    }

    isMarkerGateway(level : string) : boolean {
      return level === Config.API_AUTH_LEVEL.GLOBAL ||
        level === Config.API_AUTH_LEVEL.STORAGE ||
        level === Config.API_AUTH_LEVEL.ROLE ||
        level === Config.API_AUTH_LEVEL.TOKEN;
    }

    isAuthFailGateway(level: string) : boolean {
      return level === Config.API_AUTH_LEVEL.PRIMARY_FAILURE;
    }

    isAuthSuccessGateway(level: string) : boolean {
      return level === Config.API_AUTH_LEVEL.PRIMARY_SUCCESS;
    }

    authNavigation(useWindowed = false): Observable<string> {
      let callerUrl = document.location.protocol +'//'+ document.location.hostname + ':' + document.location.port;
      useWindowed = useWindowed ? useWindowed : callerUrl !== Config.API_ROUTE.API_URL;
      return this._authNavigation(useWindowed);
    }

    private _authNavigation(useWindowed : boolean): Observable<string> {
      let url = Config.resolveParameterLink(Config.API_ROUTE.AUTH_ENTRY_LINK, Config.API_AUTH_TYPE.LOCAL) + (useWindowed ? '/ui' : '');
      return this.http.get(url).pipe(map((data: AuthenticationResponse) => Config.API_ROUTE.API_URL + <string> data.additionalInfo ));
    }

}

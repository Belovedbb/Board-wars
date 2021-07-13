import {Inject, Injectable} from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot} from '@angular/router';
import {Observable, of} from 'rxjs';
import { AuthService } from './auth.service';
import {catchError, map} from 'rxjs/operators';
import {DOCUMENT} from "@angular/common";

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router,
              @Inject(DOCUMENT) private document: Document) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.authService.isAuthenticated()
      .pipe(
        map((response: { level: string}) => {
          if(this.authService.isMarkerGateway(response.level)){
            this.router.navigate(['pages/marker']);
            return true;
          }else if(this.authService.isAuthFailGateway(response.level)) {
            this.authService.authNavigation().map(url => this.document.location.href = url).subscribe();
          }else if(this.authService.isAuthSuccessGateway(response.level)){
            return true;
          }else{
            this.router.navigate(['pages/miscellaneous/404']);
            return true;
          }
        }),
        catchError((error) => {
          this.router.navigate(['pages/miscellaneous/404']);
         return of(true);
    }));
  }

}

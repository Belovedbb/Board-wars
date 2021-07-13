
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor, HttpResponse, HttpErrorResponse, HttpHeaderResponse, HttpSentEvent
} from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import { DOCUMENT } from '@angular/common';
import {Inject, Injectable} from "@angular/core";
import {Router} from "@angular/router";
import {map} from "rxjs/operators";
import {flatMap} from "rxjs/internal/operators";

@Injectable()
export class ReqInterceptor implements HttpInterceptor {

  constructor() {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    request = !request.withCredentials ?  request.clone({withCredentials: true}) : request;
    request = request.clone({ headers: request.headers.append('Accept', '*/*') });
    return next.handle(request);
  }

}

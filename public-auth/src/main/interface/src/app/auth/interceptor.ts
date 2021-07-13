
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor, HttpResponse, HttpErrorResponse, HttpHeaderResponse
} from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import { DOCUMENT } from '@angular/common';
import {Inject, Injectable} from "@angular/core";
import {Router} from "@angular/router";

@Injectable()
export class ReqInterceptor implements HttpInterceptor {

    constructor(private router: Router, @Inject(DOCUMENT) private document: Document) {}

    transformReq = function (body) {
        let str = [];
        for(let p in body)
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(body[p]));
        return str.join("&");
    };

    handleLoginPost = function(request: HttpRequest<any>, next: HttpHandler, me: ReqInterceptor): Observable<HttpEvent<any>> {
        request = request.clone({
            setHeaders: { "Content-Type": "application/x-www-form-urlencoded" },
            body: me.transformReq(request.body),
            withCredentials: true
        });
        return next.handle(request).map(event => {
            if (event instanceof HttpResponse) {
                if(event.status === 200) {
                    let redirect_url = event.headers.get("X-LOCAL-AUTH-REDIRECT");
                    if (redirect_url){
                        me.router.ngOnDestroy();
                        me.document.location.href = redirect_url;
                    }
                    me.router.navigate(['auth/login']);
                }
            }
            return event;
        });
    };

    handleRegisterPost = function(request: HttpRequest<any>, next: HttpHandler, me: ReqInterceptor): Observable<HttpEvent<any>> {
        request = request.clone({
            withCredentials: true
        });
        return next.handle(request).map(event => {
            if (event instanceof HttpResponse) {
                if(event.status === 201) {
                    let redirect_url = event.headers.get("X-LOCAL-AUTH-REDIRECT");
                    if (redirect_url){
                        me.router.ngOnDestroy();
                        me.document.location.href = redirect_url;
                    }
                }
            }
            return event;
        });
    };

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const pattern = ['/login', '/user/register'];
        if(request.url.search(pattern[0]) !== -1 && request.method === 'POST'){
            return this.handleLoginPost(request, next, this);
        }else if(request.url.search(pattern[1]) !== -1 && request.method === 'POST'){
            return this.handleRegisterPost(request, next, this);
        }
    return next.handle(request);
}
}
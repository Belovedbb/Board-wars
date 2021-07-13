import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import {Observable} from "rxjs";
import {catchError, timeout} from "rxjs/operators";
import {Config} from "../../config/config";

@Injectable()
export class ApiHttpService {

  constructor(private http: HttpClient) { }

  private static addCredentials(options?: any) : any{
    if(!options){
      options = {}
    }
    options.withCredentials = true;
    return options;
  }

  public get(url: string, options?: any) : Observable<any> {
    return this.http.get(url, ApiHttpService.addCredentials(options))
      .pipe(timeout(Config.NETWORK.TIMEOUT),
      catchError((err, caught) => {
        ApiHttpService.errorLogger(err);
        return err.error;
      }));
  }

  public post(url: string, data: any, options?: any) : Observable<any>{
    return this.http.post(url, data, ApiHttpService.addCredentials(options))
      .pipe(timeout(Config.NETWORK.TIMEOUT),
      catchError((err) => {
        ApiHttpService.errorLogger(err);
        return err.error;
      }));
  }

  public put(url: string, data: any, options?: any) : Observable<any>{
    return this.http.put(url, data, ApiHttpService.addCredentials(options))
      .pipe(timeout(Config.NETWORK.TIMEOUT),
      catchError((err, caught) => {
        ApiHttpService.errorLogger(err);
        return err.error;
      }));
  }

  public patch(url: string, data: any, options?: any) : Observable<any>{
    return this.http.patch(url, data, ApiHttpService.addCredentials(options))
      .pipe(timeout(Config.NETWORK.TIMEOUT),
        catchError((err, caught) => {
          ApiHttpService.errorLogger(err);
          return err.error;
        }));
  }

  public delete(url: string, options?: any) : Observable<any>{
    return this.http.delete(url, ApiHttpService.addCredentials(options))
      .pipe(timeout(Config.NETWORK.TIMEOUT),
      catchError((err, caught) => {
        ApiHttpService.errorLogger(err);
        return err.error;
      }));
  }

  private static errorLogger(err): void {
    console.info(err.message);
    console.error(err.error);
  }

}

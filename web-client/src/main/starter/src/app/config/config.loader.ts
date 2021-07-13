import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Config } from './config';


@Injectable({
  providedIn: 'root'
})
export class ConfigLoader {

  constructor(private http: HttpClient) { }

  init() {
    return this.http.get('assets/data/config.json').toPromise().then(data => {
      Config.load(data);
    });
  }

  static initializeEnvironmentConfig = (appConfig: ConfigLoader) => {
    return () => {
      return appConfig.init();
    };
  };
}

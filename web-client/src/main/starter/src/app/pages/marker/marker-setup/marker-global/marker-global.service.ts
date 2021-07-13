import { Injectable } from '@angular/core';
import {Config} from "../../../../config/config";
import {ApiHttpService} from "../../../../@core/utils";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";
import {MarkerGlobalComponent} from "./marker-global.component";
import {Messages, MessageService} from "../../../../@core/utils/message.service";

@Injectable()
export class MarkerGlobalService {
  constructor(private messageService: MessageService, private http: ApiHttpService) { }

  getGithubOrgSignUpLink(): Observable<string> {
    let url = Config.resolveParameterLink(Config.API_ROUTE.AUTH_ENTRY_LINK, Config.API_AUTH_TYPE.GITHUB) + '/ui';
    return this.http.get(url).pipe(map(data => data.additionalInfo));
  }

  registerGlobalMarker(parentRef: MarkerGlobalComponent) : void {
    const receiveMessage = event => {
      //if (event.origin !== Config.SELF_URL) {
        //return;
      //}
      const { data } = event;
      parentRef.deactivateParentView.next(false);
      if(data &&  (data.includes(Config.API_AUTH_TYPE.SUCCESS))){
        parentRef.isGlobalCompleted.next(true);
        parentRef.disableSubmit = true;
        console.log("auth successful");
      }else{
        console.log(data);
        this.messageService.errorToast("Unknown Error", Messages.generic_error);
        parentRef.isGlobalCompleted.next(false);
      }
    };
    window.removeEventListener('message', receiveMessage);

    this.getGithubOrgSignUpLink().pipe(
      map(url => {
        const features = 'width=600, height=700, toolbar=no,scrollbars=no,location=no,statusbar=no,menubar=no' +
          ',resizable=0,left = 490,top=300';
        let regWindow = window.open(Config.API_ROUTE.API_URL + url,'popup', features);
        parentRef.deactivateParentView.next(true);
        regWindow.focus();
        this.closeEvent(regWindow, parentRef);
        window.addEventListener('message', event => receiveMessage(event), false);
        return regWindow;
      })
    ).subscribe();
  }

  closeEvent(openedWindow, parent){
    let timer = setInterval(function() {
      if(openedWindow.closed) {
        parent.deactivateParentView.next(false);
        clearInterval(timer);
      }
    }, 1000);

  }

}

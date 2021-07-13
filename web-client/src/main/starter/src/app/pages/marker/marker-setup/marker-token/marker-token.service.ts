import {Injectable} from "@angular/core";
import {ApiHttpService} from "../../../../@core/utils";
import {Config} from "../../../../config/config";
import {MarkerTokenComponent} from "./marker-token.component";
import {map} from "rxjs/operators";
import {Messages, MessageService} from "../../../../@core/utils/message.service";


@Injectable()
export class MarkerTokenService {
  constructor(private messageService: MessageService, private http: ApiHttpService) { }

  registerTokenMarker(parentRef: MarkerTokenComponent) : void {
    parentRef.form.markAsDirty();
    if(parentRef.form.valid){
      let tokenForm = {
        "expiryPeriod": parentRef.tokenForm.get('expiryPeriodCtrl').value,
        "activate": parentRef.tokenForm.get('activateCtrl').value,
        "allowTokenPass": parentRef.tokenForm.get('allowTokenCtrl').value,
        "baseEmail": parentRef.tokenForm.get('baseEmailCtrl').value,
        "attachedRole": parentRef.tokenForm.get('attachedRoleCtrl').value
      };
      console.log(tokenForm);
      this.http.post(Config.API_ROUTE.AUTH_MARKER_TOKEN, tokenForm)
        .pipe(
          map((response: AuthenticationResponse) => {
            if(!response.status){
              this.handleErrorToast(response);
            }else{
              parentRef.disableSubmit = true;
              parentRef.isTokenCompleted.next(true);
            }
          })
        )
        .subscribe()
    }
  }

  handleErrorToast(response: AuthenticationResponse): void {
    if(response.value === ''){
      this.messageService.errorToast('Error', Messages.generic_error);
    }else{
      this.messageService.errorToast('Error', response.value);
    }
  }
}

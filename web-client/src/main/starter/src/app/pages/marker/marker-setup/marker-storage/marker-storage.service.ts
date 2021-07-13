import {Injectable} from "@angular/core";
import {ApiHttpService} from "../../../../@core/utils";
import {MarkerStorageComponent} from "./marker-storage.component";
import {Config} from "../../../../config/config";
import {map} from "rxjs/operators";
import {Messages, MessageService} from "../../../../@core/utils/message.service";


@Injectable()
export class MarkerStorageService {
  constructor(private messageService: MessageService, private http: ApiHttpService) { }

  registerStorageMarker(parentRef: MarkerStorageComponent) : void {
    parentRef.form.markAsDirty();
    if(parentRef.form.valid){
      let storageForm = {
        "baseLocation": parentRef.storageForm.get('locationCtrl').value,
        "allowedTypes": this.parseTypeToList(parentRef.storageForm.get('allowedTypesCtrl').value),
        "storageType": parentRef.storageForm.get('storageTypeCtrl').value
      };
      console.log(storageForm);
      this.http.post(Config.API_ROUTE.AUTH_MARKER_STORAGE, storageForm)
        .pipe(
          map((response: AuthenticationResponse) => {
            if(!response.status){
              this.handleErrorToast(response);
            }else{
              parentRef.disableSubmit = true;
              parentRef.isStorageCompleted.next(true);
            }
          })
        )
        .subscribe()
    }
  }

  parseTypeToList(rawValue : string) : string[] {
    return  rawValue.replace(/\s+/g, '').split(',');
  }

  handleErrorToast(response: AuthenticationResponse): void {
    if(response.value === ''){
      this.messageService.errorToast('Error', Messages.generic_error);
    }else{
      this.messageService.errorToast('Error', response.value);
    }
  }
}

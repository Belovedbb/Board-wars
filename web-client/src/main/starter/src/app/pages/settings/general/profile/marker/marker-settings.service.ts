import {Injectable, Injector} from "@angular/core";
import {Observable} from "rxjs";
import {ApiHttpService} from "../../../../../@core/utils";
import {filter, map} from "rxjs/operators";
import {UserService} from "../../../../management/user/user-management-service";
import {Messages, MessageService} from "../../../../../@core/utils/message.service";
import {Router} from "@angular/router";
import {ProjectService} from "../../../../kanban/board/services/project-service";
import {GlobalService} from "../../../../../@core/utils/global.service";
import {GlobalMarker, OrganizationDetail} from "../../../../marker/marker-model";
import {Config} from "../../../../../config/config";
import {MarkerSettingsComponent} from "./marker-settings.component";


@Injectable()
export class MarkerSettingsService {

  constructor(private http: ApiHttpService, private globalService: GlobalService, private projectService: ProjectService,
              private userService: UserService, private messageService: MessageService,private router: Router) {
  }

  getGlobalMarker(): Observable<GlobalMarker> {
    return this.globalService.getGlobalMarker();
  }

  submitMarkerSettingsForm(parentRef: MarkerSettingsComponent) : void {
    if(parentRef.formGroup.valid){
      let globalMarkerForm = new GlobalMarker();
      let formValue = parentRef.formGroup.value;
      globalMarkerForm.email = parentRef.marker.email;
      globalMarkerForm.organizationName = formValue.organizationName;
      globalMarkerForm.organization = new OrganizationDetail();
      globalMarkerForm.organization.description = formValue.organizationDescription;
      this.http.patch(Config.API_ROUTE.GLOBAL_MARKER, globalMarkerForm)
        .pipe(
          map((response: GlobalMarker) => {
            parentRef.marker = response;
              this.reloadComponent();
          })
        )
        .subscribe();
    }
  }

  handleErrorToast(): void {
    this.messageService.errorToast('Error', Messages.generic_error);
  }

  reloadComponent() {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigateByUrl('/pages/settings/general');
  }


}

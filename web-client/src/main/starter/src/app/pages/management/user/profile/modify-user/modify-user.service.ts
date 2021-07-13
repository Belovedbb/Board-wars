import {Injectable} from "@angular/core";
import {ApiHttpService} from "../../../../../@core/utils";
import {User, UserForm, UserResponse} from "../../../management-model";
import {map} from "rxjs/operators";
import {UserService} from "../../user-management-service";
import {Messages, MessageService} from "../../../../../@core/utils/message.service";
import {Router} from "@angular/router";
import {ModifyUserComponent} from "./modify-user.component";

@Injectable()
export class ModifyUserService {

  constructor(private http: ApiHttpService, private userService: UserService, private messageService: MessageService,private router: Router) {}

  submitModifyUserForm(parentRef: ModifyUserComponent) : void {
    if(parentRef.formGroup.valid){
      let userForm = new UserForm();
      let formValue = parentRef.formGroup.value;
      userForm.role = formValue.role;
      console.log(userForm);

      this.http.patch(parentRef.user.parent.getSelfLinkHref(), userForm)
        .pipe(
          map((response: UserResponse) => {
            if(!response.success){
              this.handleErrorToast();
            }else{
              this.reloadComponent();
            }
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
    this.router.navigateByUrl('/pages/management/user');
  }

}

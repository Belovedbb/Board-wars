import {Injectable, Injector} from "@angular/core";
import {Observable} from "rxjs";
import {ApiHttpService} from "../../../../../@core/utils";
import {User, UserForm, UserResponse} from "../../../management-model";
import {map} from "rxjs/operators";
import {ActiveUserComponent} from "./active-user.component";
import {UserService} from "../../user-management-service";
import {Messages, MessageService} from "../../../../../@core/utils/message.service";
import {Router} from "@angular/router";


@Injectable()
export class ActiveUserService {

  constructor(private http: ApiHttpService, private userService: UserService, private messageService: MessageService,private router: Router) {}


  public uploadImage(image: File, user: User): Observable<any> {
    const formData = new FormData();
    formData.append('picture', image);
    return this.http.post(user.parent.getSelfLinkHref() + '/profile-picture', formData, {responseType: 'blob'});
  }

  submitActiveUserForm(parentRef: ActiveUserComponent) : void {
    if(parentRef.formGroup.valid){
      let userForm = new UserForm();
      let formValue = parentRef.formGroup.value;
      userForm.firstName = formValue.firstName;
      userForm.otherNames = formValue.lastName;
      userForm.phoneNumber = formValue.phoneNumber;
      userForm.position = formValue.position;
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

import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {User, UserResponse} from "../management-model";
import {Observable} from "rxjs";
import {filter, map, merge, mergeAll, toArray} from "rxjs/operators";

@Injectable({ providedIn: 'root' })
export class UserService extends RestService<UserResponse> {

  constructor(injector: Injector) {
    super(UserResponse, 'management/user', injector);
  }


  getAllUsers() : Observable<User[]> {
    return this.getAll()
      .pipe(
        mergeAll(),
        filter(response => response.success),
        map(response => {
          let user = response.body;
          user.parent = response;
          user.role = this.sanitizeRole(user);
          return user;
        }),
        toArray()
      );
  }

  getFilteredUser(user: User): Observable<User> {
    return this.get(user.username)
      .pipe(
        filter(response => response.success),
        map(response => {
          let user = response.body;
          user.role = this.sanitizeRole(user);
          user.parent = response;
          return user;
        }));
  }

  sanitizeRole(user: User): string {
    let role = user.role.replace('ROLE_','');
    return role.charAt(0).toUpperCase() + role.slice(1).toLowerCase();
  }

}

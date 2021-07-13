import {Component, OnDestroy, OnInit} from '@angular/core';
import {SUB_MENU_ITEMS} from "../../pages-menu";
import {NbMenuItem} from "@nebular/theme";
import {UserService} from "./user-management-service";
import {Observable} from "rxjs";
import {User} from "../management-model";
import { map } from 'rxjs/operators';


const sidebarItems: NbMenuItem[] = [
  {
    title: 'User',
    icon: 'person-outline',
    link: '/pages/management/user',
    selected: true
  },
  {
    title: 'Team',
    icon: 'person-add-outline',
    link: '/pages/management/team',
  },
];


@Component({
  selector: 'user-management',
  styleUrls: ['./user-management.component.scss'],
  templateUrl: './user-management.component.html',
  providers: [UserService]
})
export class UserManagementComponent implements OnInit,  OnDestroy {

  users: Observable<User[]>;
  currentUser: User;

  constructor( private userService: UserService){
    this.initialize();
    this.users = userService.getAllUsers();
  }

  ngOnDestroy() {
  }

  ngOnInit(): void {
  }

  initialize(): void {
    SUB_MENU_ITEMS.length = 0;
    for (let item of sidebarItems){
      SUB_MENU_ITEMS.push(item);
    }
  }

  selectUser(user: User): void {
      this.userService.getFilteredUser(user)
        .pipe(map(user => {
        this.currentUser = user;
      })).subscribe();
  }

}

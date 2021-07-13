import { Component } from '@angular/core';
import {NbMenuItem} from "@nebular/theme";
import {SUB_MENU_ITEMS} from "../pages-menu";

const sidebarItems: NbMenuItem[] = [
  {
    title: 'User',
    icon: 'person-outline',
    link: '/pages/management/user',
  },
  {
    title: 'Team',
    icon: 'person-add-outline',
    link: '/pages/management/team',
  },
];

@Component({
  selector: 'management',
  template: `
      <router-outlet></router-outlet>
  `,
})
export class ManagementComponent {

  constructor(){
    SUB_MENU_ITEMS.length = 0;
    for (let item of sidebarItems){
      SUB_MENU_ITEMS.push(item);
    }
  }
}

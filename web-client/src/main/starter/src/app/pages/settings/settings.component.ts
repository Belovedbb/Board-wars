import { Component } from '@angular/core';
import {NbMenuItem} from "@nebular/theme";
import {SUB_MENU_ITEMS} from "../pages-menu";

const sidebarItems: NbMenuItem[] = [
];

@Component({
  selector: 'settings',
  template: `
      <router-outlet></router-outlet>
  `,
})
export class SettingsComponent {

  constructor(){
    SUB_MENU_ITEMS.length = 0;
    for (let item of sidebarItems){
      SUB_MENU_ITEMS.push(item);
    }
  }
}

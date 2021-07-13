import { Component } from '@angular/core';
import {SUB_MENU_ITEMS} from "../pages-menu";

@Component({
  selector: 'search',
  template: `
    <router-outlet></router-outlet>
  `,
})
export class SearchComponent {
  constructor() {
    SUB_MENU_ITEMS.length = 0;
  }
}

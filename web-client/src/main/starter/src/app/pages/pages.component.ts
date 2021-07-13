import { Component } from '@angular/core';

import {MENU_ITEMS, SUB_MENU_ITEMS} from './pages-menu';

@Component({
  selector: 'ngx-pages',
  styleUrls: ['pages.component.scss'],
  template: `
    <ngx-one-column-layout>
      <nb-menu slot="main" [items]="menu"></nb-menu>
      <router-outlet name='primary'></router-outlet>
      <nb-menu slot="sub" [items]="subMenu"></nb-menu>
      <router-outlet name='secondary'></router-outlet>
    </ngx-one-column-layout>
  `,
})
export class PagesComponent {
  menu = MENU_ITEMS;
  subMenu = SUB_MENU_ITEMS;
}

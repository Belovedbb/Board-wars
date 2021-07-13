import {Component, OnDestroy, OnInit} from '@angular/core';
import {SUB_MENU_ITEMS} from "../pages-menu";
import {NbMenuItem} from "@nebular/theme";

const sidebarItems: NbMenuItem[] = [
  {
    title: 'Board',
    icon: 'clipboard-outline',
    link: '/pages/kanban/board'
  },
  {
    title: 'Chart',
    icon: 'pie-chart-outline',
    link: '/pages/kanban/chart',
  }
];

@Component({
  selector: 'kanban',
  template: `
      <router-outlet></router-outlet>
  `,
})
export class KanbanComponent implements OnInit,  OnDestroy {

  private alive = true;

  constructor(){
    this.initialize();
  }

  ngOnDestroy() {
    this.alive = false;
  }

  ngOnInit(): void {}

  initialize(): void {
    SUB_MENU_ITEMS.length = 0;
    for (let item of sidebarItems){
      SUB_MENU_ITEMS.push(item);
    }
  }

}

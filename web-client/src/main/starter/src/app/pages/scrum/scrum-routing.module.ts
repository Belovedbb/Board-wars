import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {ScrumBoardComponent} from "./board/scrum-board.component";
import {ScrumComponent} from "./scrum.component";

const routes: Routes = [
  {
    path: '',
    component: ScrumComponent,
    children: [
      {
        path: 'board',
        component: ScrumBoardComponent,
      },
    ],
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
  ],
  exports: [
    RouterModule,
  ],
})
export class ScrumRoutingModule {
}


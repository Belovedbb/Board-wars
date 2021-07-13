import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {ImportGeneralComponent} from "./general/import-general.component";
import {ImportComponent} from "./import.component";

const routes: Routes = [
  {
    path: '',
    component: ImportComponent,
    children: [
      {
        path: 'general',
        component: ImportGeneralComponent,
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
export class ImportRoutingModule {
}


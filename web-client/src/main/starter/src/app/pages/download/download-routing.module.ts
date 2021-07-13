import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


import {DownloadComponent} from "./download.component";
import {DownloadGeneralComponent} from "./general/download-general.component";

const routes: Routes = [
  {
    path: '',
    component: DownloadComponent,
    children: [
      {
        path: 'general',
        component: DownloadGeneralComponent,
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
export class DownloadRoutingModule {
}


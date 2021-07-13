import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';

import { PagesComponent } from './pages.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { NotFoundComponent } from './miscellaneous/not-found/not-found.component';
import {MarkerComponent} from "./marker/marker.component";
import {AuthGuard} from "../auth/auth.guard";
import {AuthPageComponent} from "../auth/page/auth-page.component";

const routes: Routes = [{
  path: '',
  component: PagesComponent,
  children: [
    {
      path: 'dashboard',
      component: DashboardComponent,
      canActivate: [ AuthGuard ],
    },
    {
      path: 'marker',
      component: MarkerComponent,
    },
    {
      path: 'auth',
      component: AuthPageComponent,
    },
    {
      path: 'kanban',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./kanban/kanban.module')
        .then(m => m.KanbanModule),
    },
    {
      path: 'management',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./management/management.module')
        .then(m => m.ManagementModule),
    },
    {
      path: 'settings',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./settings/settings.module')
        .then(m => m.SettingsModule),
    },
    {
      path: 'scrum',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./scrum/scrum.module')
        .then(m => m.ScrumModule),
    },
    {
      path: 'search',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./search/search.module')
        .then(m => m.SearchModule),
    },
    {
      path: 'download',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./download/download.module')
        .then(m => m.DownloadModule),
    },
    {
      path: 'import',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./import/import.module')
        .then(m => m.ImportModule),
    },
    {
      path: 'miscellaneous',
      canActivate: [ AuthGuard ],
      loadChildren: () => import('./miscellaneous/miscellaneous.module')
        .then(m => m.MiscellaneousModule),
    },
    {
      path: '',
      redirectTo: 'dashboard',
      pathMatch: 'full',
    },
    {
      path: 'mark',
      redirectTo: 'marker',
      pathMatch: 'full',
    },
    {
      path: '**',
      component: NotFoundComponent,
    },
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [ AuthGuard ],
})
export class PagesRoutingModule {
}

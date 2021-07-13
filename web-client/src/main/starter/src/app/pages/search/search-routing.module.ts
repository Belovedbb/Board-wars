import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SearchComponent } from './search.component';
import { SearchModuleComponent } from './search-module/search-module.component';

const routes: Routes = [{
  path: '',
  component: SearchComponent,
  children: [{
    path: 'search-module',
    component: SearchModuleComponent,
  }],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SearchRoutingModule { }

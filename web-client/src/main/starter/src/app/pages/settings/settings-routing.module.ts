import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {SettingsComponent} from "./settings.component";
import {GeneralSettingsComponent} from "./general/general-settings.component";

const routes: Routes = [{
  path: '',
  component: SettingsComponent,
  children: [{
    path: 'general',
    component: GeneralSettingsComponent,
  }],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SettingsRoutingModule { }

export const routedComponents = [
  SettingsComponent,
  GeneralSettingsComponent
];

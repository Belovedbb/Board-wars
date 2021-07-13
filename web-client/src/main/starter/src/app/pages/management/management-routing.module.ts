import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ManagementComponent} from "./management.component";
import {TeamManagementComponent} from "./team/team-management.component";
import {UserManagementComponent} from "./user/user-management.component";

const routes: Routes = [{
  path: '',
  component: ManagementComponent,
  children: [{
    path: 'user',
    component: UserManagementComponent,
  },{
    path: 'team',
    component: TeamManagementComponent,
  }],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ManagementRoutingModule { }

export const routedComponents = [
  ManagementComponent,
  UserManagementComponent,
  TeamManagementComponent,
];

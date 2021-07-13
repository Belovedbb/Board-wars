import { NgModule } from '@angular/core';
import {
  NbBadgeModule,
  NbButtonModule,
  NbCardModule, NbInputModule,
  NbListModule,
  NbOptionModule,
  NbRadioModule, NbSelectModule,
  NbUserModule
} from '@nebular/theme';

import { ThemeModule } from '../../@theme/theme.module';
import {ManagementRoutingModule, routedComponents} from "./management-routing.module";
import {ModifyUserComponent} from "./user/profile/modify-user/modify-user.component";
import {ActiveUserComponent} from "./user/profile/active-user/active-user.component";
import {ViewUserComponent} from "./user/profile/view-user/view-user.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {OtherTeamComponent} from "./team/profile/other/other-team.component";
import {SelfTeamComponent} from "./team/profile/self/self-team.component";
import {NewTeamComponent} from "./team/profile/new/new-team.component";


@NgModule({
  imports: [
    NbCardModule,
    ThemeModule,
    ManagementRoutingModule,
    NbListModule,
    NbUserModule,
    NbRadioModule,
    NbBadgeModule,
    NbButtonModule,
    ReactiveFormsModule,
    FormsModule,
    NbOptionModule,
    NbSelectModule,
    NbInputModule,
  ],
  declarations: [
    ModifyUserComponent,
    ActiveUserComponent,
    ViewUserComponent,
    OtherTeamComponent,
    SelfTeamComponent,
    NewTeamComponent,
    ...routedComponents,
  ],
})
export class ManagementModule { }


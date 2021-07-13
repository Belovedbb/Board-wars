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
import {SettingsRoutingModule, routedComponents} from "./settings-routing.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ScrumSettingsComponent} from "./general/profile/scrum/scrum-settings.component";
import {KanbanSettingsComponent} from "./general/profile/kanban/kanban-settings.component";
import {MarkerSettingsComponent} from "./general/profile/marker/marker-settings.component";


@NgModule({
  imports: [
    NbCardModule,
    ThemeModule,
    SettingsRoutingModule,
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
    ScrumSettingsComponent,
    KanbanSettingsComponent,
    MarkerSettingsComponent,
    ...routedComponents,
  ],
})
export class SettingsModule { }


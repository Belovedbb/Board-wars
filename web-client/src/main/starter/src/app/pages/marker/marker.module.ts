import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import {MarkerComponent} from "./marker.component";
import {MarkerRoleComponent} from "./marker-setup/marker-role/marker-role.component";
import {MarkerTokenComponent} from "./marker-setup/marker-token/marker-token.component";
import {MarkerStorageComponent} from "./marker-setup/marker-storage/marker-storage.component";
import {MarkerGlobalComponent} from "./marker-setup/marker-global/marker-global.component";
import {MarkerLoaderComponent} from "./marker-setup/marker-loader/marker-loader.component";
import {
  NbAlertModule,
  NbButtonModule,
  NbCardModule, NbCheckboxModule,
  NbInputModule, NbSelectModule, NbSpinnerModule,
  NbStepperModule
} from "@nebular/theme";
import {DashboardModule} from "../dashboard/dashboard.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Ng2SmartTableModule} from "ng2-smart-table";

const COMPONENTS = [
  MarkerLoaderComponent,
  MarkerComponent,
  MarkerGlobalComponent,
  MarkerStorageComponent,
  MarkerRoleComponent,
  MarkerTokenComponent,
];

const IMPORTS = [
  FormsModule,
  ReactiveFormsModule,
  NbCardModule,
  NbButtonModule,
  NbInputModule,
  NbCardModule,
  NbStepperModule,
  CommonModule,
  NbAlertModule,
  DashboardModule,
];
const ENTRY_COMPONENTS = [
  MarkerLoaderComponent,
];

@NgModule({
  declarations: [
    ...COMPONENTS
  ],
  imports: [
    ...IMPORTS,
    NbSpinnerModule,
    NbSelectModule,
    Ng2SmartTableModule,
    NbCheckboxModule,
  ],

  entryComponents: [
    ...ENTRY_COMPONENTS
  ]
})
export class MarkerModule { }

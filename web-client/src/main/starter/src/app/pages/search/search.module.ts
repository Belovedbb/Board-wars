import { NgModule } from '@angular/core';
import {
  NbAlertModule,
  NbButtonModule,
  NbCardModule,
  NbIconModule,
  NbListModule,
  NbPopoverModule,
  NbSearchModule
} from '@nebular/theme';

import { ThemeModule } from '../../@theme/theme.module';
import { SearchRoutingModule } from './search-routing.module';
import {SearchModuleComponent} from "./search-module/search-module.component";
import {SearchComponent} from "./search.component";

const components = [
  SearchComponent,
  SearchModuleComponent,
];

@NgModule({
  imports: [
    NbCardModule,
    NbPopoverModule,
    NbSearchModule,
    NbIconModule,
    NbAlertModule,
    ThemeModule,
    SearchRoutingModule,
    NbListModule,
    NbButtonModule,
  ],
  declarations: [
    ...components,
  ],
})
export class SearchModule { }

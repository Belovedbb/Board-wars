import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import {AuthPageComponent} from "./auth-page.component";

const COMPONENTS = [
  AuthPageComponent
];

const IMPORTS = [
  CommonModule,
];
const ENTRY_COMPONENTS = [
  AuthPageComponent,
];

@NgModule({
  declarations: [
    ...COMPONENTS
  ],
    imports: [
        ...IMPORTS,
    ],

  entryComponents: [
    ...ENTRY_COMPONENTS
  ]
})
export class AuthPageModule { }

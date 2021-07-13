/**
 * @license
 * Copyright Akveo. All Rights Reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { CoreModule } from './@core/core.module';
import { ThemeModule } from './@theme/theme.module';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { ConfigLoader } from './config/config.loader';
import {
  NbChatModule,
  NbDatepickerModule,
  NbDialogModule,
  NbMenuModule,
  NbSidebarModule,
  NbToastrModule,
  NbWindowModule,
} from '@nebular/theme';
import {ReqInterceptor} from "./@core/intercept/interceptor";
import {MessageService} from "./@core/utils/message.service";
import {NgxHalClientModule} from "@lagoshny/ngx-hal-client";
import {ExternalConfigurationService} from "./@core/utils/hateoas.service";

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    AppRoutingModule,
    NbSidebarModule.forRoot(),
    NbMenuModule.forRoot(),
    NbDatepickerModule.forRoot(),
    NbDialogModule.forRoot(),
    NbWindowModule.forRoot(),
    NbToastrModule.forRoot(),
    NbChatModule.forRoot({
      messageGoogleMapKey: 'AIzaSyA_wNuCzia92MAmdLRzmqitRGvCF7wCZPY',
    }),
    CoreModule.forRoot(),
    ThemeModule.forRoot(),
    NgxHalClientModule.forRoot()
  ],
  providers: [
    ConfigLoader,
    {
    provide: APP_INITIALIZER,
    useFactory: ConfigLoader.initializeEnvironmentConfig,
    multi: true,
    deps: [ConfigLoader]
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ReqInterceptor,
      multi: true
    },
    {
      provide: 'ExternalConfigurationService',
      useClass: ExternalConfigurationService
    },
    MessageService,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
}

import {Component, OnDestroy, OnInit} from '@angular/core';
import {SUB_MENU_ITEMS} from "../../pages-menu";
import {NbMenuItem} from "@nebular/theme";
import {GeneralSettingsService} from "./general-settings-service";
import {SettingsModule, SettingsModuleConstant} from "../settings-model";

const sidebarItems: NbMenuItem[] = [

];


@Component({
  selector: 'general-settings',
  styleUrls: ['./general-settings.component.scss'],
  templateUrl: './general-settings.component.html',
})
export class GeneralSettingsComponent implements OnInit,  OnDestroy {

  settingsModules: SettingsModule[];
  currentSettingsModule: SettingsModule;

  constructor( private generalSettingsService: GeneralSettingsService){
    this.initialize();
    this.settingsModules = generalSettingsService.getAllSettingsModules();
  }

  ngOnDestroy() {
  }

  ngOnInit(): void {
  }

  initialize(): void {
    SUB_MENU_ITEMS.length = 0;
    for (let item of sidebarItems){
      SUB_MENU_ITEMS.push(item);
    }
  }

  selectModule(moduleType: SettingsModuleConstant): void {
    for (let i of this.settingsModules) {
      if( i.type == moduleType) {
        this.currentSettingsModule = i;
      }
    }
  }

}

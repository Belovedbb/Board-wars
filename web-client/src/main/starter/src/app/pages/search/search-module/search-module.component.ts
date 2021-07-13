import { Component } from '@angular/core';
import {SearchModule, SearchModuleConstant} from "../../search/search-model";
import {SearchModuleService} from "../../search/search-module/search-module-service";
import {SUB_MENU_ITEMS} from "../../pages-menu";
import {NbMenuItem} from "@nebular/theme";

const sidebarItems: NbMenuItem[] = [

];


@Component({
    selector: 'search-module',
  styleUrls: ['./search-module.component.scss'],
    templateUrl: 'search-module.component.html',
})
export class SearchModuleComponent {

  searchModules: SearchModule[];
  currentSearchModule: SearchModule;

  constructor( private searchModuleService: SearchModuleService){
    this.initialize();
    this.searchModules = searchModuleService.getAllSearchModules();
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

  selectModule(moduleType: SearchModuleConstant): void {
    for (let i of this.searchModules) {
      if( i.type == moduleType) {
        this.currentSearchModule = i;
      }
    }
  }

}

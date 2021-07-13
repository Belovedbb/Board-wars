import { Component } from '@angular/core';
import {SUB_MENU_ITEMS} from "../../pages-menu";

@Component({
  selector: 'import-general',
  styleUrls: ['./import-general.component.scss'],
  templateUrl: './import-general.component.html',
})
export class ImportGeneralComponent {

  constructor() {
    SUB_MENU_ITEMS.length = 0;
  }
}

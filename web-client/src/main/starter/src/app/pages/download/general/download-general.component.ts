import { Component } from '@angular/core';
import {SUB_MENU_ITEMS} from "../../pages-menu";

@Component({
  selector: 'download-general',
  styleUrls: ['./download-general.component.scss'],
  templateUrl: './download-general.component.html',
})
export class DownloadGeneralComponent {

  constructor() {
    SUB_MENU_ITEMS.length = 0;
  }
}

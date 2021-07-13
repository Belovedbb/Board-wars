import { Component } from '@angular/core';
import {SUB_MENU_ITEMS} from "../../pages-menu";

@Component({
  selector: 'scrum-board',
  styleUrls: ['./scrum-board.component.scss'],
  templateUrl: './scrum-board.component.html',
})
export class ScrumBoardComponent {

  constructor() {
    SUB_MENU_ITEMS.length = 0;
  }
}

import { Component, OnInit} from '@angular/core';
import {ScrumSettingsService} from "./scrum-settings.service";

@Component({
  selector: 'scrum-settings-general',
  templateUrl: './scrum-settings.component.html',
  styleUrls: ['./scrum-settings.component.scss'],
  providers: [ScrumSettingsService]
})
export class ScrumSettingsComponent implements OnInit {


  constructor( private scrumSettingsService: ScrumSettingsService) { }

  ngOnInit() {

  }

}

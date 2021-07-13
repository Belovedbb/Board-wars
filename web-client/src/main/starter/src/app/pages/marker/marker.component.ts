import { Component } from '@angular/core';
import {NbDialogService} from "@nebular/theme";
import {MarkerLoaderComponent} from "./marker-setup/marker-loader/marker-loader.component";

@Component({
  selector: 'marker-section',
  styleUrls: ['./marker.component.scss'],
  templateUrl: './marker.component.html',
})

export class MarkerComponent {

  constructor(private dialogService: NbDialogService) {}

  ngOnInit() {
    this.open();
  }
  open() {
    this.dialogService.open(MarkerLoaderComponent, {
      context: {
        title: 'Configuration',
      },
      hasBackdrop: false,
      closeOnEsc: false,
      autoFocus: true,
      hasScroll: true,
    });
  }

}

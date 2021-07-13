import { Component, OnInit, Input } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MarkerSettingsService} from "./marker-settings.service";
import {GlobalMarker} from "../../../../marker/marker-model";

@Component({
  selector: 'marker-settings-general',
  templateUrl: './marker-settings.component.html',
  styleUrls: ['./marker-settings.component.scss'],
  providers: [MarkerSettingsService]
})
export class MarkerSettingsComponent implements OnInit {

  formGroup: FormGroup;

  marker: GlobalMarker;

  constructor( private markerSettingsService: MarkerSettingsService, private formBuilder: FormBuilder) {

  }

  ngOnInit() {
    this.formGroup = this.formBuilder.group({
      organizationName: [''],
      organizationDescription: [''],
    });
    this.markerSettingsService.getGlobalMarker()
      .subscribe(marker => {
        this.marker = marker;
        if(marker) {
          this.formGroup.get('organizationName').setValue(marker.organizationName);
          this.formGroup.get('organizationDescription').setValue(marker.organization ? marker.organization.description : '');
        }
      })
  }

  onSubmit(formData) {
    this.markerSettingsService.submitMarkerSettingsForm(this);
  }
}

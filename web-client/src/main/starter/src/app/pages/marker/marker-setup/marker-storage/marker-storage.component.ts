import {Component, Host, Input, OnInit} from '@angular/core';
import {
  AbstractControl,
  ControlContainer,
  FormBuilder,
  FormGroup,
  FormGroupDirective,
  Validators
} from "@angular/forms";
import {MarkerStorageService} from "./marker-storage.service";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'marker-storage',
  styleUrls: ['./marker-storage.component.scss'],
  templateUrl: './marker-storage.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: FormGroupDirective }],
  providers: [MarkerStorageService]
})
export class MarkerStorageComponent implements OnInit{

  @Input() isStorageCompleted: BehaviorSubject<boolean>;
  disableSubmit: boolean = false;
  public form: FormGroup;
  public storageForm : AbstractControl;
  private fb: FormBuilder = new FormBuilder();
  storageTypes = [{'key' : 'FILESYSTEM', 'value' : 'File System'}];

  constructor(private markerStorageService: MarkerStorageService,
              @Host() private parentFor: FormGroupDirective) {}

  ngOnInit(): void {
    this.isStorageCompleted.next(false);
    this.form = this.parentFor.form;
    this.form.addControl('storageControl', this.fb.group({
      locationCtrl: ['', Validators.required],
      allowedTypesCtrl: ['', Validators.required],
      storageTypeCtrl : ['', Validators.required]
    }));
    this.storageForm = this.form.controls.storageControl;
    this.storageForm.get('storageTypeCtrl').setValue(this.storageTypes[0].key)
  }

  onStorageSubmit() {
    this.markerStorageService.registerStorageMarker(this);
  }

}

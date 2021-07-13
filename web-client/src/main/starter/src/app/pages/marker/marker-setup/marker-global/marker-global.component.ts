import {Component, Host, Input, OnInit} from '@angular/core';
import {ControlContainer, FormBuilder, FormGroup, FormGroupDirective, Validators} from "@angular/forms";
import {NbDialogService} from "@nebular/theme";
import {MarkerGlobalService} from "./marker-global.service";
import {BehaviorSubject, of} from "rxjs";
import {map, tap} from "rxjs/operators";


@Component({
  selector: 'marker-global',
  styleUrls: ['./marker-global.component.scss'],
  templateUrl: './marker-global.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: FormGroupDirective }],
  providers: [MarkerGlobalService]
})
export class MarkerGlobalComponent implements OnInit{

  public form: FormGroup;
  private fb: FormBuilder = new FormBuilder();
  disableSubmit: boolean = false;
  @Input() deactivateParentView: BehaviorSubject<boolean>;
  @Input() isGlobalCompleted: BehaviorSubject<boolean>;

  constructor(private dialogService: NbDialogService,
              @Host() private parentFor: FormGroupDirective,
              private globalService: MarkerGlobalService) {}

  ngOnInit(): void {
    this.isGlobalCompleted.next(false);
    this.form = this.parentFor.form;
    this.form.addControl('child', this.fb.group({
      globalCtrl: ''
    }))
  }

  openGithubOrgRegister() {
    this.globalService.registerGlobalMarker(this);
  }


}

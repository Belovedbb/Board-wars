import {
  AfterContentChecked,
  AfterViewChecked,
  Component,
  DoCheck,
  Host,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import {
  AbstractControl,
  ControlContainer,
  FormBuilder,
  FormGroup,
  FormGroupDirective,
  Validators
} from "@angular/forms";
import {MarkerTokenService} from "./marker-token.service";
import {BehaviorSubject} from "rxjs";
import {RoleEntity} from "../../marker-model";

@Component({
  selector: 'marker-token',
  styleUrls: ['./marker-token.component.scss'],
  templateUrl: './marker-token.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: FormGroupDirective }],
  providers: [MarkerTokenService]
})
export class MarkerTokenComponent implements OnInit, AfterContentChecked{

  @Input() isTokenCompleted: BehaviorSubject<boolean>;
  @Input() roleEntities: BehaviorSubject<RoleEntity[]>;
  private shouldSetValue: boolean = true;
  disableSubmit: boolean = false;
  public form: FormGroup;
  private fb: FormBuilder = new FormBuilder();
  tokenForm : AbstractControl;

  constructor(private markerTokenService: MarkerTokenService,
              @Host() private parentFor: FormGroupDirective) {}

  ngOnInit(): void {
    this.isTokenCompleted.next(false);
    this.form = this.parentFor.form;
    this.form.addControl('tokenControl', this.fb.group({
      expiryPeriodCtrl: ['', Validators.required],
      activateCtrl: ['', Validators.required],
      allowTokenCtrl: ['', Validators.required],
      baseEmailCtrl: ['', Validators.required],
      attachedRoleCtrl: ['', Validators.required]
    }));
    this.tokenForm = this.form.controls.tokenControl;
  }

  onTokenSubmit() {
    this.markerTokenService.registerTokenMarker(this);
  }

  ngAfterContentChecked(): void {
    if(this.roleEntities.getValue().length > 0 && this.shouldSetValue){
      this.tokenForm.get('attachedRoleCtrl').setValue(this.roleEntities.getValue()[0].entityName);
      this.shouldSetValue = false;
    }
  }

}

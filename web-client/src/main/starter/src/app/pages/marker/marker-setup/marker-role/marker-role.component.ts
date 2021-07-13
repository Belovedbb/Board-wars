import {Component, Host, Input, OnInit} from '@angular/core';
import {
  AbstractControl,
  ControlContainer,
  FormBuilder,
  FormGroup,
  FormGroupDirective,
  Validators
} from "@angular/forms";
import {LocalDataSource} from "ng2-smart-table";
import {MarkerRoleService} from "./marker-role.service";
import {RoleEntity, Validation} from "../../marker-model";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'marker-role',
  styleUrls: ['./marker-role.component.scss'],
  templateUrl: './marker-role.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: FormGroupDirective }],
  providers: [MarkerRoleService]
})
export class MarkerRoleComponent implements OnInit{

  @Input() isRoleCompleted: BehaviorSubject<boolean>;
  @Input() entities: BehaviorSubject<RoleEntity[]>;
  disableSubmit: boolean = false;
  public form: FormGroup;
  roleForm : AbstractControl;
  private fb: FormBuilder = new FormBuilder();
  source: LocalDataSource = new LocalDataSource();
  validationError : Validation[][] = [];

  settings = this.markerRoleService.getRoleHeader();

  constructor(private markerRoleService: MarkerRoleService,
              @Host() private parentFor: FormGroupDirective) {
    this.source.load([markerRoleService.generateDefaultEntity(this.source)]);
  }

  ngOnInit(): void {
    this.isRoleCompleted.next(false);
    this.form = this.parentFor.form;
    this.form.addControl('roleControl', this.fb.group({
      activateCtrl: ['', Validators.required]
    }))
    this.roleForm = this.form.controls.roleControl;
  }

  onRoleSubmit() {
    this.markerRoleService.registerRoleMarker(this);
  }

  onDeleteConfirm(event): void {
    if (window.confirm('Are you sure you want to delete?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  addTable(event): void {
    this.source.add(this.markerRoleService.generateDefaultEntity(this.source));
    this.source.refresh();
  }
}

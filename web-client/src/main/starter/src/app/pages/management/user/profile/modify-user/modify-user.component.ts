import { Component, OnInit, Input } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {User} from "../../../management-model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ModifyUserService} from "./modify-user.service";

@Component({
  selector: 'profile-modify-user',
  templateUrl: './modify-user.component.html',
  styleUrls: ['./modify-user.component.scss'],
  providers: [ModifyUserService]
})
export class ModifyUserComponent implements OnInit {

  @Input() user: User;
  file: File;
  class = 'hide-text';
  firstName: string;
  lastName: string;
  formGroup: FormGroup;
  roleTypes = [
    {key : 'ROLE_ADMIN', value : 'Admin'},
    {key : 'ROLE_CUSTOM', value : 'Custom'},
    {key : 'ROLE_VISITOR', value : 'Visitor'},
  ];

  constructor( private modifyUserService: ModifyUserService, private formBuilder: FormBuilder) { }

  ngOnInit() {
    const names: string [] =  (this.user.firstName + ' '+ this.user.otherNames ).split(' ');
    this.firstName = names[0];
    this.lastName = names[1];
    this.formGroup = this.formBuilder.group({
      role: ['', Validators.required],
    });
    let key = this.roleTypes[0].key;
    for(let i of this.roleTypes){
      if(i.value === this.user.role) {key = i.key; break;}
    }
    this.formGroup.get('role').setValue(key);
  }

  onSubmit() {
    this.modifyUserService.submitModifyUserForm(this);
  }

}

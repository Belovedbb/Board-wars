import { Component, OnInit, Input } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {PictureSnippet, User} from "../../../management-model";
import {ActiveUserService} from "./active-user.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'profile-active-user',
  templateUrl: './active-user.component.html',
  styleUrls: ['./active-user.component.scss'],
  providers: [ActiveUserService]
})
export class ActiveUserComponent implements OnInit {

  @Input() user: User;
  file: File;
  originalProfilePic = true;
  class = 'hide-text';
  firstName: string;
  lastName: string;
  formGroup: FormGroup;
  roleTypes = [
    {key : 'ROLE_ADMIN', value : 'Admin'},
    {key : 'ROLE_CUSTOM', value : 'Custom'},
    {key : 'ROLE_VISITOR', value : 'Visitor'},
  ];

  selectedFile: PictureSnippet;
  private reader = new FileReader();

  constructor(private sanitizer: DomSanitizer, private activeUserService: ActiveUserService, private formBuilder: FormBuilder) {
    this.selectedFile = new PictureSnippet(null, null);
    this.onPictureSuccess();
  }

  processProfilePicture(e: any) {
    const file: File = e.target.files[0];
    this.reader.addEventListener('load', (event: any) => {
      this.selectedFile = new PictureSnippet(event.target.result, file);
      this.selectedFile.pending = true;
      this.activeUserService.uploadImage(this.selectedFile.file, this.user).subscribe(
        (res: Blob) => {
          this.onPictureSuccess();
          this.originalProfilePic = false;
          this.file = file;
        },
        (err) => {
          this.onPictureError();
        })
    });
    this.reader.readAsDataURL(file);
  }

  private onPictureSuccess() {
    this.selectedFile.pending = false;
    this.selectedFile.status = 'ok';
  }

  private onPictureError() {
    this.selectedFile.pending = false;
    this.selectedFile.status = 'fail';
    this.selectedFile.src = '';
  }

  ngOnInit() {
    const names: string [] =  (this.user.firstName + ' '+ this.user.otherNames ).split(' ');
    this.firstName = names[0];
    this.lastName = names[1];
    this.formGroup = this.formBuilder.group({
      firstName: [this.firstName, [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      lastName: [this.lastName],
      position: [this.user.position],
      role: ['', Validators.required],
    });
    let key = this.roleTypes[0].key;
    for(let i of this.roleTypes){
      if(i.value === this.user.role) {key = i.key; break;}
    }
    this.formGroup.get('role').setValue(key);
  }

  get filePreview(): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl((
      window.URL.createObjectURL(this.file)));
  }

  imageHover() {
    this.class = 'show-text';
  }

  imageLeave() {
    this.class = 'hide-text';
  }

  onSubmit(formData) {
    this.activeUserService.submitActiveUserForm(this);
  }
}

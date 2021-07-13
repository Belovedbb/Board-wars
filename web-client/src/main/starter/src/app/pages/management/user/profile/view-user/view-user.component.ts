import { Component, OnInit, Input } from '@angular/core';
import {User} from "../../../management-model";

@Component({
  selector: 'profile-view-user',
  templateUrl: './view-user.component.html',
  styleUrls: ['./view-user.component.scss'],
})
export class ViewUserComponent implements OnInit {

  @Input() user: User;
  file: File;
  originalProfilePic = true;
  class = 'hide-text';
  firstName: string;
  lastName: string;

  constructor() { }

  ngOnInit() {
    const names: string [] =  (this.user.firstName + ' '+ this.user.otherNames ).split(' ');
    this.firstName = names[0];
    this.lastName = names[1];
  }

}

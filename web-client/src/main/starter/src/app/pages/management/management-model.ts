import {Resource} from "@lagoshny/ngx-hal-client";

interface Response {
  success: boolean;
  body: object;
  serverTime: string;
}

export class PictureSnippet {
  pending: boolean = false;
  status: string = 'init';
  constructor(public src: string, public file: File) {}
}

export class User{
  username: string;
  firstName: string;
  otherNames: string;
  role: string;
  position: string;
  teams: Team[];
  profilePictureLink: string;
  operation: string;
  parent: UserResponse;

}

export class UserForm {
  phoneNumber: string;
  firstName: string;
  otherNames: string;
  position: string;
  role: string;
}

export class UserResponse extends Resource implements Response {
  body: User;
  serverTime: string;
  success: boolean;
}

export class Team {
  name: string;
  code: string;
  colorCode: string;
  active: boolean;
  leader: User;
  parent: TeamResponse;
  members: User[];
  dateCreated: Date;
  description: string;
  isLeader: boolean;
}

export class TeamForm {
  name: string;
  colorCode: string;
  active: boolean;
  members: User[];
  description: string;
  leader: User;
}

export class TeamResponse extends Resource implements Response {
  body: Team;
  serverTime: string;
  success: boolean;
}

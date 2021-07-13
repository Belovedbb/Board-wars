
export class GlobalMarker {
   applicationName: string;
   organizationName: string;
   fullName: string;
   email: string;
   organization: OrganizationDetail;
}

export class OrganizationDetail {
  login: string;
  url: string;
  avatarUrl: string;
  description: string;
  reposUrl: string;
}

export class Validation {
  get keyValue() : string {
    return this.key;
  }

  get messageValue(): string {
    return this.message;
  }

  constructor(private key: string, private message: string) {
  }
}

export class RoleEntity{

  getFieldError() : Validation[] {
    let validations = [];
    if(!this.id){
      validations.push(new Validation('id', ' cannot be empty'));
    }else{
      if(!this.attribute){
        validations.push(new Validation('attribute', ' at id ' + this.id + ' cannot be empty'));
      }
      if (!this.name) {
        validations.push(new Validation('name', ' at id ' + this.id + ' cannot be empty'));
      }
      if(!this.semantic){
        validations.push(new Validation('semantic', ' at id ' + this.id + ' cannot be empty'));
      }
      if(!this.precedence){
        validations.push(new Validation('precedence', ' at id ' + this.id + ' cannot be empty'));
      }
    }
    return validations;
  }

  get entityName(){
    return this.name
  }

  set entityName(value : string){
     this.name = value;
  }

  get entityAttribute() {
    return this.attribute;
  }

  set entityAttribute(value: string) {
    this.attribute = value;
  }

  get entitySemantic() {
    return this.semantic;
  }

  set entitySemantic(value: string) {
     this.semantic = value;
  }

  get entityPrecedence() {
    return this.precedence;
  }

  set entityPrecedence(value: number) {
     this.precedence = value;
  }

  get entityId() {
    return this.id;
  }

  set entityId(value: number) {
     this.id = value;
  }

  constructor(
    private id: number,
    private name : string,
    private attribute: string,
    private semantic: string,
    private precedence: number
  ) {
  }
}

import {Injectable} from "@angular/core";
import {ApiHttpService} from "../../../../@core/utils";
import {MarkerRoleComponent} from "./marker-role.component";
import {DataSource} from "ng2-smart-table/lib/lib/data-source/data-source";
import {RoleEntity} from "../../marker-model"
import {Config} from "../../../../config/config";
import {Messages, MessageService} from "../../../../@core/utils/message.service";
import {map} from "rxjs/operators";

@Injectable()
export class MarkerRoleService {
  constructor(private messageService: MessageService, private http: ApiHttpService) {
  }

  public generateDefaultEntity(source: DataSource) : RoleEntity{
    return new RoleEntity(source.count() + 1, '', 'Project', 'Create', 1)
  }
  public getRoleHeader() {
    return {
      hideSubHeader: true,
      edit: {
        editButtonContent: '<i class="nb-edit"></i>',
        saveButtonContent: '<i class="nb-checkmark"></i>',
        cancelButtonContent: '<i class="nb-close"></i>',
      },
      delete: {
        deleteButtonContent: '<i class="nb-trash"></i>',
        confirmDelete: true,
      },
      columns: {
        id: {
          title: 'ID',
          type: 'number',
        },
        name: {
          title: 'Name',
          type: 'string',
        },
        attribute: {
          title: 'Attribute',
          type: 'html',
          editor: {
            type: 'list',
            config: {
              list: [
                { value: 'Project', title: 'Project' },
                { value: 'Board', title: 'Board' },
                {value: 'Task', title: 'Task',}
              ],
            },
          },
        },
        semantic: {
          title: 'Semantic',
          type: 'html',
          editor: {
            type: 'list',
            config: {
              list: [
                { value: 'Create', title: 'Create' },
                { value: 'View', title: 'View' },
                { value: 'Modify', title: 'Modify'}
              ],
            },
          },
        },
        precedence: {
          title: 'Precedence',
          type: 'number',
        },
      },
    };
  }
  registerRoleMarker(parentRef: MarkerRoleComponent) : void {
    parentRef.form.markAsDirty();
    parentRef.validationError = [];

    parentRef.source.getAll().then(raw => {
      let entities: RoleEntity[] = [];
      raw.forEach(element => {
        let entity = new RoleEntity(element.id, element.name, element.attribute, element.semantic, element.precedence);
        let errorList = entity.getFieldError();
        if(errorList && errorList.length > 0){
          parentRef.validationError.push(errorList);
        }else{
          entities.push(entity);
        }
      });
      if(parentRef.form.valid && parentRef.validationError && parentRef.validationError.length === 0){
        let roleForm = {
          "activate": parentRef.roleForm.get('activateCtrl').value,
          "roleEntities": this.sanitizeRoleEntity(entities),
        };
        console.log(roleForm);
        this.http.post(Config.API_ROUTE.AUTH_MARKER_ROLE, roleForm)
          .pipe(
            map((response: AuthenticationResponse) => {
              if(response.status !== true){
                this.handleErrorToast(response);
              }else{
                parentRef.disableSubmit = true;
                parentRef.entities.next(entities);
                parentRef.isRoleCompleted.next(true);
              }
            })
          )
          .subscribe()
      }else{
        entities = [];
      }
    });
  }

  handleErrorToast(response: AuthenticationResponse): void {
    if(response.value === ''){
      this.messageService.errorToast('Error', Messages.generic_error);
    }else{
      this.messageService.errorToast('Error', response.value);
    }
  }

  sanitizeRoleEntity(entities : RoleEntity[]) : RoleEntity[] {
    for (let entity of entities) {
      entity.entityAttribute = entity.entityAttribute.toUpperCase();
      entity.entitySemantic = entity.entitySemantic.toUpperCase();
    }
    return entities;
  }
}

import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {Observable} from "rxjs";
import {Column, ColumnResponse, Project} from "../../kanban-model";
import {HateoasUtil} from "../../../../@core/utils/hateoas.service";
import {TaskService} from "./task-service";

@Injectable({ providedIn: 'root' })
export class ColumnService extends RestService<ColumnResponse> {

  constructor(injector: Injector, private taskService: TaskService) {
    super(ColumnResponse, 'kanban/column', injector);
  }


  getAllUsers() : Observable<Column[]> {
    return null
  }

  getFilteredUser(Column: Column): Observable<Column> {
    return null;
  }

  loadColumnToDomain(data: any, isEmbedded: boolean = false) : Column {
    if(!data) return null;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    let tasksData: any = data.tasks;
    let newData : Column = <Column>data;
    newData.tasks = this.taskService.loadTaskArrayToDomain(tasksData, true);
    return data;
  }

  loadColumnArrayToDomain(data: any, isEmbedded: boolean = false) : Column[] {
    let columns: Column[] = [];
    if(!data) return columns;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    for(let column of data) {
      columns.push(this.loadColumnToDomain(column, false));
    }
    return columns;
  }



}

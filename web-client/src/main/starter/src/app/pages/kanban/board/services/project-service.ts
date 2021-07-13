import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {Observable} from "rxjs";
import {ColumnResponse, Project, ProjectResponse} from "../../kanban-model";
import {filter, map, mergeAll, toArray} from "rxjs/operators";
import {ColumnService} from "./column-service";
import {HateoasUtil} from "../../../../@core/utils/hateoas.service";

@Injectable({ providedIn: 'root' })
export class ProjectService extends RestService<ProjectResponse> {

  constructor(injector: Injector, private columnService: ColumnService) {
    super(ProjectResponse, 'kanban/project', injector);
  }


  getAllProjects() : Observable<Project[]> {
    return this.getAll()
      .pipe(
        mergeAll(),
        filter(response => response.success),
        map((response: ProjectResponse) => {
          return this.loadProjectToDomain(response.body, false);
        }),
        toArray()
      );
  }

  mapColumnResponseToColumn(data) : ColumnResponse {
    return null;
  }

  getFilteredProject(project: Project): Observable<Project> {
    return this.get(project.code).pipe(
      map((response: ProjectResponse) => {
      return this.loadProjectToDomain(response.body, false);
    }));
  }

  public loadProjectToDomain(data: any, isEmbedded: boolean = false): Project {
    if(!data) return null;
    if(isEmbedded) {
      data = HateoasUtil.trimEmbedded(data);
    }
    let columnsData: any = data.columns;
    let newData : Project = <Project>data;
    newData.columns = this.columnService.loadColumnArrayToDomain(columnsData, true);
    return data;
  }

}

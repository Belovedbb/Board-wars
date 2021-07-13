import {Injectable, Injector} from "@angular/core";
import {RestService} from "@lagoshny/ngx-hal-client";
import {
  GanttResponse,
  Project,
  ProjectResponse
} from "../../kanban-model";
import {Observable, of} from "rxjs";
import {filter, map, mergeAll, toArray} from "rxjs/operators";
import {ProjectService} from "../../board/services/project-service";

@Injectable({ providedIn: 'root' })
export class OverviewChartService {

  constructor( private projectService: ProjectService) {

  }

  getAllProjects(): Observable<Project[]> {
    return this.projectService.getAllProjects()
  }

  getProject(code: number): Observable<Project> {
    return this.projectService.get(code).pipe(
      filter((projectResponse) => projectResponse.success === true),
      map((projectResponse: ProjectResponse) => this.projectService.loadProjectToDomain(projectResponse.body)));
  }

}

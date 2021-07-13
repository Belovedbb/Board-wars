import {Injectable} from "@angular/core";
import {SearchModule, SearchModuleConstant} from "../search-model";

@Injectable({ providedIn: 'root' })
export class SearchModuleService  {

  constructor() {
  }


  getAllSearchModules() : SearchModule[] {
    let list = [];
    list.push(new SearchModule("Kanban", SearchModuleConstant.KANBAN));
    list.push(new SearchModule("Marker", SearchModuleConstant.MARKER));
    list.push(new SearchModule("Management", SearchModuleConstant.MANAGEMENT));
    list.push(new SearchModule("Scrum", SearchModuleConstant.SCRUM));
    return list;
  }

}

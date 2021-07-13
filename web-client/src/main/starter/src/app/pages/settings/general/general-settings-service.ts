import {Injectable} from "@angular/core";
import {SettingsModule, SettingsModuleConstant} from "../settings-model";

@Injectable({ providedIn: 'root' })
export class GeneralSettingsService  {

  constructor() {
  }


  getAllSettingsModules() : SettingsModule[] {
    let list = [];
    list.push(new SettingsModule("Kanban", SettingsModuleConstant.KANBAN));
    list.push(new SettingsModule("Marker", SettingsModuleConstant.MARKER));
    list.push(new SettingsModule("Scrum", SettingsModuleConstant.SCRUM));
    return list;
  }

}

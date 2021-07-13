interface Response {
  success: boolean;
  body: object;
  serverTime: string;
}

export enum SettingsModuleConstant {
  KANBAN,
  MARKER,
  SCRUM,
}

export class SettingsModule {
  type: SettingsModuleConstant;
  name: string;
  constructor(n: string, t: SettingsModuleConstant) {
    this.name = n; this.type = t;
  }
}

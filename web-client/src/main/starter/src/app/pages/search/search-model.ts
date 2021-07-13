
export enum SearchModuleConstant {
  KANBAN,
  MARKER,
  MANAGEMENT,
  SCRUM,
}

export class SearchModule {
  type: SearchModuleConstant;
  name: string;
  constructor(n: string, t: SearchModuleConstant) {
    this.name = n; this.type = t;
  }
}

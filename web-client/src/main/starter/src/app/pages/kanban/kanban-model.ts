import {Resource} from "@lagoshny/ngx-hal-client";
import {Team} from "../management/management-model";

interface Response {
  success: boolean;
  body: object;
  serverTime: string;
}

export class TeamUser {
  type: string;
  identity: string;
  pictureUrl: string;
}
export class Project {
  name: string;
  active: boolean;
  description: string;
  code: number;
  start: Date;
  end: Date;
  teamUser: TeamUser;
  swimLanes: SwimLane[];
  columns: Column[];
}

export class ProjectForm {
  description: string;
  name: string;
  status: string;
  endPeriod: Date;
  teamUser: TeamUser;
  tags: string[];
}

export class ProjectResponse extends Resource implements Response {
  body: Project;
  serverTime: string;
  success: boolean;
}

export class SwimLane {
  name: string;
}

export class Column {
  name: string;
  description: string;
  color: string;
  taskLimit: number;
  tasks: Task[];
}

export class ColumnForm {
  description: string;
  name: string;
  color: string;
  taskLimit: number;
}

export class ColumnResponse extends Resource implements Response {
  body: Column;
  serverTime: string;
  success: boolean;
}

export  class Category {
  name: string;
  order: number;
}

export class Tags {
  values: string[];
}

export class Task {
  position: number;
  name: string;
  description: string;
  startDate: Date;
  endDate: Date;
  attachments: File[];
  tags: string[];
  categories: string[];
  assignee: TeamUser;
  reporter: TeamUser;
  createdDate: Date;
  updatedDate: Date;
  comments: TaskComment[];
  subTasks: SubTask[];
}

export class TaskForm {
  name: string;
  description: string;
  startDate: Date;
  endDate: Date;
  assignee: TeamUser;
  tags: string[];
  categories: string[];
}

export class TaskResponse extends Resource implements Response {
  body: Task;
  serverTime: string;
  success: boolean;
}

export class TaskComment {
  code: number;
  comment: string;
  timeCreated: Date;
  teamUser: TeamUser;
  reply: boolean;
}

export  class TaskCommentResponse extends Resource implements Response {
  body: TaskComment;
  serverTime: string;
  success: boolean;
}

export class TaskCommentForm {
  comment: string;
  teamUser: TeamUser;
  timeCreated: Date;
}

export class SubTask {
  code: number;
  description: string;
  startDate: Date;
  endDate: Date;
  status: string;
}

export  class SubTaskResponse extends Resource implements Response {
  body: SubTask;
  serverTime: string;
  success: boolean;
}
/*
private String comment;
    private TeamUserPayload teamUser;
    private LocalDateTime timeCreated;
 */
export const enum OPERATION {
  NEW ,
  EDIT,
  VIEW ,

}

export const enum CategoryPart {
  DANGER = "Danger",
  HIGH = "High",
  AVERAGE = "Average",
  SUBTLE = "Subtle",
  LOW = "Low"
}

export class CategoryType {
  part: CategoryPart;
  color: string;
  constructor(private categoryType: CategoryPart, private colorType: string) {
    this.part = categoryType;this.color = colorType;
  }
}

export enum Transfer{
  IN,
  OUT
}

export class BoardData {
  column: Column;
  tasks: Task[];
  constructor(c: Column, ts: Task[]) {
    this.column = c;
    this.tasks = ts;
  }
}

//chart
export class FlowDiagram {
  column: Column;
  currentTime: Date;
  taskSize: number;
}

export class CFD {
  projectCode: number;
  flows: FlowDiagram[];
}

export class CFDData2 {
  currentTime: Date;
  taskSize: number;
}

export class CFDChartData {
  name: string;
  color: string;
  data: CFDData2[];
  type: string;
  showInLegend: boolean;
  marker: any;
}

export class CFDResponse extends Resource implements Response {
  body: CFD;
  serverTime: string;
  success: boolean;
}

export class Gantt {
  id: string;
  name: string;
  start: Date;
  end: Date;
  dependency: string;
}

export class GanttResponse extends Resource implements Response {
  body: CFD;
  serverTime: string;
  success: boolean;
}

export class OverviewOrder{
  type: string;
  key: string;
}

export class OverviewData {
  color: string;
  label: string;
  value: any;
}

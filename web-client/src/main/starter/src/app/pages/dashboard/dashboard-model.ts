import {Resource} from "@lagoshny/ngx-hal-client";
import {Observable} from "rxjs";
import {Project} from "../kanban/kanban-model";

interface Response {
  success: boolean;
  body: object;
  serverTime: string;
}

export class TimeLineSnippetBody {
  tag: string;
  content: string;
}

export class TimeLineSnippet {
  header: string;
  datetime: Date;
  user: string;
  body: TimeLineSnippetBody[];
  type: TimelineType;
  footer: string;
}

export class History {
  id: string;
  title: string;
  message: string;
  eventPeriod: Date;
  category: string;
  username: string;
  type: string;
  data: any;
}

export class HistoryResponse extends Resource implements Response {
  body: History;
  serverTime: string;
  success: boolean;
}
export enum HistoryType {
  ALL,
  KANBAN,
  SCRUM,
  GRAPHICS,
  MANAGEMENT
}

export enum TimelineType {
  CREATE,
  UPDATE,
  DELETE,
  LOG
}



export interface Month {
  month: string;
  projectCount: string;
  down: boolean;
  taskCount: string;
}

export interface ActivityFrequency {
  title: string;
  active?: boolean;
  months: Month[];
}

export interface ActivityFrequencyChart {
  label: string;
  value: number;
}

export abstract class ActivityFrequencyData {
  abstract getListData(): Observable<ActivityFrequency[]>;
  abstract getChartData(): Observable<ActivityFrequencyChart[]>;
}

export class ActivityFrequencyResponse implements Response {
  body: ActivityFrequency;
  serverTime: string;
  success: boolean;
}

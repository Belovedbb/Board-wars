import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {HistoryType} from "../dashboard-model";

@Component({
  selector: 'dashboard-history',
  styleUrls: ['./history.component.scss'],
  templateUrl: './history.component.html',
})
export class HistoryComponent {
  @Input() canShow : boolean;
  @Input() historyType: HistoryType;
}

import {AfterViewInit, Component, OnInit} from '@angular/core';
import {animate, state, style, transition, trigger} from "@angular/animations";

@Component({
  selector: 'kanban-chart',
  styleUrls: ['./chart.component.scss'],
  templateUrl: './chart.component.html',
  animations: [
    trigger('changeState', [
      state('state1', style({
        transform: 'scale(0)'
      })),
      state('state2', style({
        transform: 'scale(1)'
      })),
      transition('state1=>state2', animate('500ms'))
    ]),
    trigger('changeStatePanel', [
      state('statePanel1', style({
        transform: 'scale(0)'
      })),
      state('statePanel2', style({
        transform: 'scale(1)'
      })),
      transition('*=>statePanel2', animate('500ms'))
    ])
  ]
})
export class KanbanChartComponent implements OnInit {

  currentState: string;
  currentPanelState: string;

  charts = [
    {'type': 'overview', 'key': 'ov', 'name': 'Overview Chart'},
    {'type': 'gantt', 'key': 'gt', 'name': 'Gantt Chart'},
    {'type': 'cfd', 'key': 'cfd', 'name': 'Cumulative Flow Diagram'}
    ];

  currentChart: string = this.charts[0].key;

  endState() {
    this.currentState = "state2";
  }

  endStatePanel() {
    this.currentPanelState = "statePanel2";
  }

  constructor() { }

  ngOnInit() {
    this.currentState = "state1";
    this.currentPanelState = "statePanel1";
  }

  onChartSelect(key: string) : void {
    this.currentChart = key;
    this.currentState = "state1";
  }

}

import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import * as Highcharts from 'highcharts';
import {CfdChartService} from "./cfd-chart.service";
import {map} from "rxjs/operators";
import {Project} from "../../kanban-model";
import {Observable, of} from "rxjs";
import {flatMap} from "rxjs/internal/operators";

@Component({
  selector: 'kanban-cfd-chart',
  styleUrls: ['./cfd-chart.component.scss'],
  templateUrl: './cfd-chart.component.html',
})
export class CFDChartComponent implements OnInit,AfterViewInit{
  @ViewChild('parent') public chartEl: ElementRef;

  projects: Observable<Project[]> = null;
  selectedProject: Observable<Project> = null;

  constructor(private cfdChartService: CfdChartService) {
    this.projects = this.cfdChartService.getAllProjects();
  }

  ngOnInit(): void {

  }

  ngAfterViewInit() {
    this.projects.pipe(
      map((projs: Project[]) => {
        if(projs && projs.length > 0) {
          this.selectedProject = of(projs[0]);
          return projs[0];
        }
      }),
      flatMap(proj => this.cfdChartService.getDataOption(proj.code)
        .pipe(map((options: any) => Highcharts.chart(this.chartEl.nativeElement, options))))
    ).subscribe();

  }

  onProjectChange(code: number): void {
    this.selectedProject = this.cfdChartService.getProject(code);
    this.cfdChartService.getDataOption(code)
      .pipe(map((options: any) => Highcharts.chart(this.chartEl.nativeElement, options))).subscribe()
  }

}

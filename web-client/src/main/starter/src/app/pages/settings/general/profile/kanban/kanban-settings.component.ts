import { Component, OnInit, Input } from '@angular/core';
import {KanbanSettingsService} from "./kanban-settings.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs";
import {Column, Gantt, Project} from "../../../../kanban/kanban-model";
import {map} from "rxjs/operators";

@Component({
  selector: 'kanban-settings-general',
  templateUrl: './kanban-settings.component.html',
  styleUrls: ['./kanban-settings.component.scss'],
  providers: [KanbanSettingsService]
})
export class KanbanSettingsComponent implements OnInit {

  projects: Observable<Project[]> = null;
  selectedProject: Observable<Project> = null;
  selectedProjectLink: string = null;
  columns: Column[] = [];
  selectedColumn: Column = null;
  selectedColumnLink: string = null;

  formGroup: FormGroup;

  constructor( private kanbanSettingsService: KanbanSettingsService, private formBuilder: FormBuilder) {
    this.projects = this.kanbanSettingsService.getAllProjects();
  }

  ngOnInit() {
    this.formGroup = this.formBuilder.group({
      projectName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      projectDescription: [''],
      columnColor: [''],
      columnTaskLimit: [''],
      columnDescription: [''],
    });
  }

  onProjectChange(code: number): void {
    this.selectedProject = this.kanbanSettingsService.getProject(code, this);
    this.selectedColumn = null;
    this.selectedProject.pipe(
      map(project => {
        this.formGroup.get('projectName').setValue(project.name);
        this.formGroup.get('projectDescription').setValue(project.description);
        this.columns = project.columns;
      })
    ).subscribe();
  }

  getColumn( value):Column {
    return  this.columns.find(obj => {
      return obj.name === value
    })
  }

  onColumnChange(name: string) : void {
    this.selectedProject.pipe(
      map(project => {
        let column = this.getColumn(name);
        this.selectedColumnLink = this.selectedProjectLink + '/column/' + column.name;
          this.formGroup.get('columnColor').setValue(column.color);
        this.formGroup.get('columnTaskLimit').setValue(column.taskLimit);
        this.formGroup.get('columnDescription').setValue(column.description);
        this.selectedColumn = column;
      })
    ).subscribe()
  }

  onSubmit(formData) {
    this.kanbanSettingsService.submitActiveUserForm(this);
  }
}

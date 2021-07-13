import {AfterViewChecked, ChangeDetectionStrategy, Component, Inject} from '@angular/core';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from "@angular/cdk/drag-drop";
import * as eva from 'eva-icons';
import {DOCUMENT} from "@angular/common";
import {CategoryPart, Column, OPERATION, Project, Task, Transfer} from "../kanban-model";
import {BoardService} from "./board-service";
import {ProjectService} from "./services/project-service";
import {TaskService} from "./services/task-service";
import {ColumnService} from "./services/column-service";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {NbDialogService} from "@nebular/theme";
import {SubBoardComponent} from "./sub/sub-board.component";
import {TaskSubBoardComponent} from "./task-sub/task-sub-board.component";
import {flatMap} from "rxjs/internal/operators";


@Component({
  selector: 'kanban-board',
  styleUrls: ['./board.component.scss'],
  templateUrl: './board.component.html',
  providers: [BoardService, ProjectService, TaskService, ColumnService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KanbanBoardComponent implements AfterViewChecked{

  board: Observable<Project[]> = null;
  selectedProject: Observable<Project> = null;
  deactivateMainComponentView = new BehaviorSubject(false);

  constructor(private dialogService: NbDialogService, @Inject(DOCUMENT) private document: Document, private boardService: BoardService){
  }

  ngOnInit() {
    this.board = this.boardService.loadBoardProject().pipe(
      map((projects: Project[]) => {
        this.selectedProject = of(projects && projects.length > 0 ? projects[0] : null);
        return projects;
    }));
  }

  drop(event: CdkDragDrop<Task[]>, columnLimit: number, project: Observable<Project>, column: Column, prevColumnName: string) {
    ///{position}/move-to/column/{transfer-column-name}/task/{transfer-task-position}
    if(event.container.data?.length >= columnLimit)
      return;
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      project.pipe(
        flatMap(proj => this.boardService.moveTask(event, columnLimit, proj, column, prevColumnName, Transfer.IN)),
        map(bool => {
          if(!bool) {
            this.boardService.showRevertMessage();
            this.ngOnInit();
          }
        })
      ).subscribe();
    } else {
      transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
      project.pipe(
        flatMap(proj => this.boardService.moveTask(event, columnLimit, proj, column, prevColumnName, Transfer.OUT)),
        map(bool => {
          if(!bool) {
            this.boardService.showRevertMessage();
            this.ngOnInit();
          }
        })
      ).subscribe();
    }
  }

  //TODO clean up
  ngAfterViewChecked(): void {
    let tags  = this.document.getElementsByTagName('svg');
    for (let i = 0; i < tags.length; i++) {
      let tag = tags[i];
      if (tag.classList.contains('eva')) {
        tag.classList.add("eva-animation");
        tag.classList.add('eva-icon-hover-zoom');
      }
    }
  }

  onProjectChange(code: number): void {
    this.selectedProject = this.boardService.getProject(code);
  }

  onProjectAddClicked() {
    this.deactivateMainComponentView.next(true);
    this.dialogService.open(SubBoardComponent, {
      context: {
        title: 'New Project',
        type: 'NEW_PROJECT',
        parent: this,
      },
      hasBackdrop: true,
      closeOnEsc: false,
      autoFocus: true,
      hasScroll: true,
    }).onClose.subscribe(() => {
      this.deactivateMainComponentView.next(false);
    });
  }

  canDisplayAddTask(column: Column): boolean {
    if(column){
      if(!column.tasks && column.taskLimit > 0) return true;
      if(column.tasks && column.tasks.length < column.taskLimit) return true;
    }
    return false;
  }

  onNewTask(project: Observable<Project>, column: Column) {
    this.onTaskOperation(project, column, null, 'New Task',  OPERATION.NEW);
  }

  onEditTask(project: Observable<Project>, column: Column, task: Task){
    this.onTaskOperation(project, column, task, 'Edit Task',  OPERATION.EDIT);
  }

  onViewTask(project: Observable<Project>, column: Column, task: Task){
    this.onTaskOperation(project, column, task,'View Task',  OPERATION.VIEW);
  }

  private onTaskOperation(project: Observable<Project>, column: Column, task: Task, title: string, operation: OPERATION) {
    this.deactivateMainComponentView.next(true);
    this.dialogService.open(TaskSubBoardComponent, {
      context: {
        title: title,
        type: operation,
        parent: this,
        project: project,
        column: column,
        task: task,
      },
      hasBackdrop: true,
      closeOnEsc: false,
      autoFocus: true,
      hasScroll: true,
    }).onClose.subscribe(() => {
      this.deactivateMainComponentView.next(false);
    });
  }

  formatDate(date: Date): string {
    return this.boardService.formatDate(date);
  }

  getCategoryColorOutline(task: Task) : string {
    if(task) {
      switch(task.categories[0]){
        case CategoryPart.LOW: return "task-blue-up-double";
        case CategoryPart.SUBTLE: return "task-blue-up";
        case CategoryPart.AVERAGE: return "task-green-up";
        case CategoryPart.HIGH: return "task-red-up";
        case CategoryPart.DANGER: return "task-red-up-double";
      }
    }
    return "";
  }

  getCategoryIcon(task: Task) : string {
    if(task) {
      switch(task.categories[0]){
        case CategoryPart.LOW: return "task-body-blue-down-double";
        case CategoryPart.SUBTLE: return "task-body-blue-down";
        case CategoryPart.AVERAGE: return "task-body-green-dough";
        case CategoryPart.HIGH: return "task-body-red-up";
        case CategoryPart.DANGER: return "task-body-red-up-double";
      }
    }
    return "";
  }

}

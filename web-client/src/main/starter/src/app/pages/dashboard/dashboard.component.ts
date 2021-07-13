import {AfterViewInit, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {NbThemeService} from '@nebular/theme';
import {takeWhile} from 'rxjs/operators';
import {HistoryType} from "./dashboard-model";
import {Stat} from "./status-card/status-card.component";
import {SUB_MENU_ITEMS} from "../pages-menu";

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
  index: number;
}

@Component({
  selector: 'ngx-dashboard',
  styleUrls: ['./dashboard.component.scss'],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit, OnDestroy, AfterViewInit {
  private alive = true;
  canShow = true;
  historyType = HistoryType.ALL;

  allCard: CardSettings = {
    title: 'ALL',
    iconClass: 'nb-lightbulb',
    type: 'primary',
    index: 0
  };
  kanbanCard: CardSettings = {
    title: 'KANBAN',
    iconClass: 'nb-roller-shades',
    type: 'success',
    index: 1
  };
  scrumCard: CardSettings = {
    title: 'SCRUM',
    iconClass: 'nb-audio',
    type: 'info',
    index: 2
  };
  graphicsCard: CardSettings = {
    title: 'GRAPHICS',
    iconClass: 'nb-coffee-maker',
    type: 'warning',
    index: 3
  };
  managementCard: CardSettings = {
    title: 'MANAGEMENT',
    iconClass: 'nb-coffee-maker',
    type: 'primary',
    index: 4
  };

  statusCards: CardSettings[];
  status = [];
  data;
  commonStatusCardsSet: CardSettings[] = [
    this.allCard,
    this.kanbanCard,
    this.scrumCard,
    this.graphicsCard,
    this.managementCard
  ];

  statusCardsByThemes: {
    default: CardSettings[];
    cosmic: CardSettings[];
    corporate: CardSettings[];
    dark: CardSettings[];
  } = {
    default: this.commonStatusCardsSet,
    cosmic: this.commonStatusCardsSet,
    corporate: [
      {
        ...this.allCard,
        type: 'warning',
        index: 0
      },
      {
        ...this.kanbanCard,
        type: 'primary',
        index: 1
      },
      {
        ...this.scrumCard,
        type: 'danger',
        index: 2
      },
      {
        ...this.graphicsCard,
        type: 'info',
        index: 3
      },{
        ...this.managementCard,
        type: 'primary',
        index: 4
      },
    ],
    dark: this.commonStatusCardsSet,
  };

  constructor(private themeService: NbThemeService, private cdRef: ChangeDetectorRef) {
    SUB_MENU_ITEMS.length = 0;
  }

  ngAfterViewInit(): void {
    setTimeout(()=>{
      this.themeService.getJsTheme()
        .pipe(takeWhile(() => this.alive))
        .subscribe(theme => {
          this.statusCards = this.statusCardsByThemes[theme.name];
          this.cdRef.detectChanges();
        });
    },0)

  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.alive = false;
  }

  loadButtonPage(element: CardSettings): void {
    let stat: Stat = this.status[element.index];
    this.canShow = stat.on;
    switch (element.index) {
      case 0: {
        this.historyType = HistoryType.ALL;
        break;
      }
      case 1: {
        this.historyType = HistoryType.KANBAN;
        break;
      }
      case 2: {
        this.historyType = HistoryType.SCRUM;
        break;
      }
      case 3: {
        this.historyType = HistoryType.GRAPHICS;
        break;
      }
      case 4: {
        this.historyType = HistoryType.MANAGEMENT;
        break;
      }
    }
  }

}

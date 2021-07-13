import {Component, Input, OnInit} from '@angular/core';
export class Stat{
  title: string;
  on: boolean;
}

@Component({
  selector: 'ngx-status-card',
  styleUrls: ['./status-card.component.scss'],
  template: `
    <nb-card (click)="sList[index].on = onClickStat()" [ngClass]="{'off': !sList[index].on}">
      <div class="list-cursor icon-container icon status-{{ type }}">
            <h5 >{{ title }}</h5>
          <ng-content></ng-content>
      </div>
    </nb-card>
  `,
})
export class StatusCardComponent implements OnInit{

  @Input() title: string;
  @Input() type: string;
  //@Input() on = true;
  @Input() index;
  @Input() sList : Stat[] ;


  onClickStat() : boolean {
    for(let s of this.sList) {
      if(s.title !== this.title) {
        s.on = true;
      }
    }
    return !this.sList[this.index].on;
  }

  ngOnInit(): void {
    let stat = new Stat();
    stat.title = this.title;
    stat.on = true;
    this.sList.push(stat);
  }
}

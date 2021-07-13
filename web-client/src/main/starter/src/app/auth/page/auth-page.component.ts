import { Component } from '@angular/core';

@Component({
  selector: 'auth-page-section',
  styleUrls: ['./auth-page.component.scss'],
  templateUrl: './auth-page.component.html',
})

export class AuthPageComponent {

  constructor() {}

  ngOnInit() {
    this.open();
  }

  open() {
    const params = window.location.search;
    if (window.opener) {
      window.opener.postMessage(params);
      window.close();
    }
  }

}

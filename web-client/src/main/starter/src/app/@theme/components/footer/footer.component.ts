import { Component } from '@angular/core';

@Component({
  selector: 'ngx-footer',
  styleUrls: ['./footer.component.scss'],
  template: `
    <span class="created-by">
      Created with ♥ by <b><a href="https://akveo.page.link/8V2f" target="_blank">Beloved</a></b> 2021
    </span>
    <div class="socials">
      <a href="https://github.com/belovedbb" target="_blank" class="ion ion-social-github"></a>
      <a href="https://twitter.com/beloved_johnny" target="_blank" class="ion ion-social-twitter"></a>
      <a href="https://linkedin.com/beloved" target="_blank" class="ion ion-social-linkedin"></a>
    </div>
  `,
})
export class FooterComponent {
}

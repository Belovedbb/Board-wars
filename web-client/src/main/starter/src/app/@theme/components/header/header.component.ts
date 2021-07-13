import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {NB_WINDOW, NbMediaBreakpointsService, NbMenuService, NbSidebarService, NbThemeService} from '@nebular/theme';

import {ApiHttpService, LayoutService} from '../../../@core/utils';
import {map, takeUntil, filter, mergeMap, flatMap} from 'rxjs/operators';
import { Subject } from 'rxjs';
import {Config} from "../../../config/config";
import {AuthService} from "../../../auth/auth.service";
import {DOCUMENT} from "@angular/common";
import {GlobalService} from "../../../@core/utils/global.service";
import {User} from "../../../pages/management/management-model";

@Component({
  selector: 'ngx-header',
  styleUrls: ['./header.component.scss'],
  templateUrl: './header.component.html',
})
export class HeaderComponent implements OnInit, OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  userPictureOnly: boolean = false;
  user: User;

  themes = [
    {
      value: 'default',
      name: 'Light',
    },
    {
      value: 'dark',
      name: 'Dark',
    },
  ];

  currentTheme = 'default';

  userMenu = [
    {
      title: 'Profile',
      icon: 'radio-button-on-outline',
      link: '/pages/management/user'
    },
    {
      title: 'Settings',
      icon: 'settings-2-outline',
      link: '/pages/settings/general'
    },
    {
      title: 'Log out',
      icon: 'log-out-outline',
      data: 'logout',
    }
    ];

  userMenuTag = "userMenuTag";

  themeSwitcherConfig = {
    value: true,
    disabled: false,
    color: {
      checked: '#222b45',
      unchecked: '#9ea6bf',
    },
    switchColor: {
      checked: '#3366FF',
      unchecked: '#3366FF',
    },
    labels: {
      unchecked: this.themes[0].name,
      checked: this.themes[1].name,
    },
    height: 25,
    width: 65,
  };

  constructor(private sidebarService: NbSidebarService,
              private menuService: NbMenuService,
              private themeService: NbThemeService,
              private layoutService: LayoutService,
              private breakpointService: NbMediaBreakpointsService,
              private authService: AuthService,
              private http: ApiHttpService,
              private globalService: GlobalService,
              @Inject(DOCUMENT) private document: Document) {
  }

  ngOnInit() {
    this.menuService.onItemClick()
      .pipe(
        filter(({ tag }) => tag === this.userMenuTag),
        map(({ item: { data } }) => data),
        filter(data =>  data === 'logout'),
        mergeMap(() => this.http.get(Config.API_ROUTE.LOGOUT)),
        mergeMap( () => this.authService.authNavigation().map(url => this.document.location.href = url)),
      )
      .subscribe();

    this.currentTheme = this.themeService.currentTheme;
    this.globalService.isLoggedIn().pipe(
      takeUntil(this.destroy$),
      filter(res => res),
      flatMap(res => this.globalService.getLoggedInUser())
    ).subscribe((user: User) => this.user = user);

    const { xl } = this.breakpointService.getBreakpointsMap();
    this.themeService.onMediaQueryChange()
      .pipe(
        map(([, currentBreakpoint]) => currentBreakpoint.width < xl),
        takeUntil(this.destroy$),
      )
      .subscribe((isLessThanXl: boolean) => this.userPictureOnly = isLessThanXl);

    this.themeService.onThemeChange()
      .pipe(
        map(({ name }) => name),
        takeUntil(this.destroy$),
      )
      .subscribe(themeName => this.currentTheme = themeName);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
  changeTheme(toggleResponse: boolean) {
    toggleResponse ?
      this.themeService.changeTheme(this.themes[0].value) : this.themeService.changeTheme(this.themes[1].value);
  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');
    this.layoutService.changeLayoutSize();

    return false;
  }

  navigateHome() {
    this.menuService.navigateHome();
    return false;
  }
}

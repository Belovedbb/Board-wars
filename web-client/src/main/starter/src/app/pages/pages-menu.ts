import { NbMenuItem } from '@nebular/theme';

export const MENU_ITEMS: NbMenuItem[] = [
  {
    title: 'Activity',
    icon: 'activity-outline',
    link: '/pages/dashboard',
    home: true,
  },
  {
    title: 'Kanban',
    icon: 'layout-outline',
    link: '/pages/kanban/board',
  },
  {
    title: 'Scrum',
    icon: 'npm-outline',
    link: '/pages/scrum/board',
  },
  {
    title: 'Search',
    icon: 'search-outline',
    link: '/pages/search/search-module',
  },
  {
    title: 'User Management',
    icon: 'people-outline',
    link: '/pages/management/user',
  },
  {
    title: 'Settings',
    icon: 'settings-2-outline',
    link: '/pages/settings/general',
  },
  {
    title: 'PLUGINS',
    group: true,
  },
  {
    title: 'Import',
    icon: 'upload-outline',
    link: '/pages/import/general',
  },
  {
    title: 'Download',
    icon: 'download-outline',
    link: '/pages/download/general',
  },
];


export let SUB_MENU_ITEMS: NbMenuItem[] = [
];

import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', loadComponent: () => import('./home/home.component').then(m => m.HomeComponent) },
  { path: 'programs', loadComponent: () => import('./programs/programs.component').then(m => m.ProgramsComponent) },
  { path: 'store', loadComponent: () => import('./store/store.component').then(m => m.StoreComponent) },
  { path: 'settings', loadComponent: () => import('./settings/settings.component').then(m => m.SettingsComponent) },
  { path: 'logs', loadComponent: () => import('./logs/logs.component').then(m => m.LogsComponent) },
  { path: 'plugin/:id', loadComponent: () => import('./plugin/plugin.component').then(m => m.PluginComponent) },
];


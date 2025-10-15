import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { MenuItem } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { ThemeService } from './theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, MenubarModule],
  template: `
    <p-menubar [model]="items">
      <ng-template pTemplate="end">
        <button class="p-button p-button-text" (click)="toggleTheme()" title="Toggle theme">
          <i class="pi" [ngClass]="{'pi-sun': theme.theme.includes('dark'), 'pi-moon': theme.theme.includes('light')}"></i>
        </button>
      </ng-template>
    </p-menubar>
    <div class="p-3">
      <router-outlet></router-outlet>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  items: MenuItem[] = [
    { label: 'Home', routerLink: ['/home'], icon: 'pi pi-home' },
    { label: 'Programs', routerLink: ['/programs'], icon: 'pi pi-cog' },
    { label: 'Store', routerLink: ['/store'], icon: 'pi pi-shopping-cart' },
    { label: 'Settings', routerLink: ['/settings'], icon: 'pi pi-sliders-h' },
    { label: 'Logs', routerLink: ['/logs'], icon: 'pi pi-book' }
  ];
  constructor(public theme: ThemeService) {}
  toggleTheme() { this.theme.toggle(); }
}

import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [CardModule, ButtonModule],
  template: `
    <p-card header="Welcome">
      <p>App Platform + Target Platform demo UI.</p>
      <button pButton type="button" label="Go to Programs" routerLink="/programs" icon="pi pi-cog"></button>
    </p-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent {}


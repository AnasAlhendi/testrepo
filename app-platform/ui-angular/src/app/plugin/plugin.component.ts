import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CardModule } from 'primeng/card';
import { ActivatedRoute } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-plugin',
  imports: [CardModule],
  template: `
    <p-card [header]="'Plugin: ' + (id)">
      <p>Details coming soon.</p>
    </p-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PluginComponent {
  id = this.route.snapshot.paramMap.get('id');
  constructor(private route: ActivatedRoute) {}
}


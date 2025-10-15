import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-logs',
  imports: [CommonModule, FormsModule, CardModule, ButtonModule],
  template: `
    <p-card header="Logs">
      <div class="flex align-items-center gap-2 mb-3">
        <select [(ngModel)]="target">
          <option value="server">Server</option>
          <option value="program:core-service">Program: core-service</option>
        </select>
        <button pButton label="Tail" (click)="tail()"></button>
      </div>
      <pre style="max-height: 400px; overflow: auto; background: #111; color: #0f0; padding: 1rem;">{{text()}}</pre>
    </p-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LogsComponent implements OnInit {
  text = signal('');
  target = 'server';

  ngOnInit() { this.tail(); }

  async tail() {
    try {
      const res = await fetch('/api/tp/logs?target=' + encodeURIComponent(this.target) + '&lines=200');
      if (!res.ok) return;
      this.text.set((await res.json()).join('\n'));
    } catch {}
  }
}

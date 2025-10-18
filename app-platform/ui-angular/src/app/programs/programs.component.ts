import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-programs',
  imports: [CommonModule, TableModule, ButtonModule],
  template: `
    <p-table
      [value]="rows()"
      [paginator]="true"
      [rows]="10"
      [scrollable]="true"
      scrollHeight="400px"
      [virtualScroll]="true"
      [virtualScrollItemSize]="44">
      <ng-template pTemplate="header">
        <tr>
          <th>ID</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-r>
        <tr>
          <td>{{ r.id }}</td>
          <td>{{ r.status }}</td>
          <td>
            <button pButton size="small" icon="pi pi-play" (click)="start(r.id)"></button>
            <button class="ml-2" pButton size="small" icon="pi pi-stop" (click)="stop(r.id)"></button>
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgramsComponent implements OnInit {
  rows = signal<any[]>([]);

  async ngOnInit() {
    await this.reload();
  }

  async reload() {
    try {
      const res = await fetch('/api/tp/programs');
      if (!res.ok) return;
      this.rows.set(await res.json());
    } catch {}
  }

  async start(id: string) {
    await fetch('/api/tp/programs/start?id=' + encodeURIComponent(id), { method: 'POST' });
    await this.reload();
  }
  async stop(id: string) {
    await fetch('/api/tp/programs/stop?id=' + encodeURIComponent(id), { method: 'POST' });
    await this.reload();
  }
}

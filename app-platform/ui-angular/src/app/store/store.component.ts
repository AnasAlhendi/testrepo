import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-store',
  imports: [CommonModule, CardModule, TableModule, ButtonModule],
  template: `
    <p-card header="Registry Store">
      <div class="flex align-items-center gap-2 mb-3">
        <button pButton label="Sync from Registry" icon="pi pi-refresh" (click)="sync()"></button>
      </div>

      <h3>Programs</h3>
      <p-table [value]="programs()" [paginator]="true" [rows]="10">
        <ng-template pTemplate="header">
          <tr><th>ID</th><th>Version</th><th>Actions</th></tr>
        </ng-template>
        <ng-template pTemplate="body" let-r>
          <tr>
            <td>{{ r.id }}</td>
            <td>{{ r.version }}</td>
            <td>
              <button pButton size="small" label="Install" icon="pi pi-download" (click)="installProgram(r)"></button>
            </td>
          </tr>
        </ng-template>
      </p-table>

      <h3 class="mt-4">Plugins</h3>
      <p-table [value]="plugins()" [paginator]="true" [rows]="10">
        <ng-template pTemplate="header">
          <tr><th>ID</th><th>Version</th><th>Actions</th></tr>
        </ng-template>
        <ng-template pTemplate="body" let-r>
          <tr>
            <td>{{ r.id }}</td>
            <td>{{ r.version }}</td>
            <td>
              <button pButton size="small" label="Install" icon="pi pi-download" (click)="installPlugin(r)"></button>
            </td>
          </tr>
        </ng-template>
      </p-table>
    </p-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StoreComponent implements OnInit {
  programs = signal<any[]>([]);
  plugins = signal<any[]>([]);

  async ngOnInit() { await this.reload(); }

  async reload() {
    try {
      const res = await fetch('/api/registry/catalog.json');
      if (!res.ok) return;
      const cat = await res.json();
      this.programs.set(cat.programs || []);
      this.plugins.set(cat.plugins || []);
    } catch {}
  }

  async installProgram(r: any) {
    const spec = {
      id: r.id,
      version: r.version,
      source: r.url ? { url: r.url } : r.maven ? { maven: r.maven } : (r.path ? { path: r.path } : null)
    };
    await fetch('/api/tp/programs/install', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(spec) });
  }

  async installPlugin(r: any) {
    const spec = {
      id: r.id,
      version: r.version,
      source: r.url ? { url: r.url } : r.maven ? { maven: r.maven } : (r.path ? { path: r.path } : null)
    };
    await fetch('/api/tp/plugins/install', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(spec) });
  }

  async sync() {
    await fetch('/api/tp/registry/sync', { method: 'POST' });
    // Optionally reload catalog in case registry recomputed versions
    await this.reload();
  }
}

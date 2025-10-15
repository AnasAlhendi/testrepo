import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';

@Component({
  standalone: true,
  selector: 'app-settings',
  imports: [CardModule, InputTextModule, ButtonModule],
  template: `
    <p-card header="Settings">
      <div class="p-fluid formgrid grid">
        <div class="field col-12 md:col-6">
          <label>Registry URL</label>
          <input pInputText placeholder="http://localhost:8082/api" />
        </div>
      </div>
      <button pButton label="Save" icon="pi pi-save"></button>
    </p-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsComponent {}


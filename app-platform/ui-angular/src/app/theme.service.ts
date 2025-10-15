import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private current = 'lara-light-blue';
  get theme() { return this.current; }
  setTheme(name: string) {
    const link = document.getElementById('theme-css') as HTMLLinkElement | null;
    if (link) {
      link.href = `https://unpkg.com/primeng/resources/themes/${name}/theme.css`;
      this.current = name;
    }
  }
  toggle() { this.setTheme(this.current.includes('light') ? this.current.replace('light', 'dark') : this.current.replace('dark', 'light')); }
}


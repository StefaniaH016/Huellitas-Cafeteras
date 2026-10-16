import { Component, signal } from '@angular/core';
import { TitleCasePipe } from '@angular/common';

type AdminSection =
  'resumen' | 'validaciones' | 'moderacion' | 'usuarios' | 'casos' | 'catalogos' | 'reportes';

@Component({
  selector: 'app-panel-admin',
  imports: [TitleCasePipe],
  templateUrl: './panel-admin.component.html',
  styleUrl: './panel-admin.component.css',
})
export class PanelAdminComponent {
  activeSection = signal<AdminSection>('resumen');

  setSection(section: AdminSection): void {
    this.activeSection.set(section);
  }
}

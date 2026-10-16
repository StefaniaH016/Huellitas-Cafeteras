import { Component, signal } from '@angular/core';
import { TitleCasePipe } from '@angular/common';

type VetSection = 'solicitudes' | 'pacientes' | 'historial' | 'informes' | 'mensajes' | 'perfil';

@Component({
  selector: 'app-panel-veterinaria',
  imports: [TitleCasePipe],
  templateUrl: './panel-veterinaria.component.html',
  styleUrl: './panel-veterinaria.component.css',
})
export class PanelVeterinariaComponent {
  activeSection = signal<VetSection>('solicitudes');

  setSection(section: VetSection): void {
    this.activeSection.set(section);
  }
}

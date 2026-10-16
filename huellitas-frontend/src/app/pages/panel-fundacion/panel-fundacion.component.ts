import { Component, signal } from '@angular/core';
import { TitleCasePipe } from '@angular/common';

type SideSection = 'resumen' | 'custodia' | 'zona' | 'medicas' | 'adopcion' | 'mensajes' | 'perfil';

@Component({
  selector: 'app-panel-fundacion',
  imports: [TitleCasePipe],
  templateUrl: './panel-fundacion.component.html',
  styleUrl: './panel-fundacion.component.css',
})
export class PanelFundacionComponent {
  activeSection = signal<SideSection>('resumen');

  setSection(section: SideSection): void {
    this.activeSection.set(section);
  }
}

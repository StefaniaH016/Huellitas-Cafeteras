import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { CasoService } from '../../core/services/caso.service';
import { UsuarioService } from '../../core/services/usuario.service';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

type SideSection = 'resumen' | 'custodia' | 'zona' | 'medicas' | 'adopcion' | 'mensajes' | 'perfil';

@Component({
  selector: 'app-panel-fundacion',
  imports: [CommonModule, TitleCasePipe],
  templateUrl: './panel-fundacion.component.html',
  styleUrl: './panel-fundacion.component.css',
})
export class PanelFundacionComponent implements OnInit {
  private casoService = inject(CasoService);
  private usuarioService = inject(UsuarioService);

  activeSection = signal<SideSection>('resumen');
  resumen = signal<{ enCustodia: number; esperandoAtencion: number; emergencias: number }>({
    enCustodia: 0,
    esperandoAtencion: 0,
    emergencias: 0,
  });
  perfil = signal<ApiResponse>(null);
  cargando = signal(false);
  errorMensaje = signal('');
  casos = signal<ApiResponse[]>([]);

  ngOnInit(): void {
    this.cargarDatos();
  }

  setSection(section: SideSection): void {
    this.activeSection.set(section);
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    // Cargar perfil
    this.usuarioService.getMiPerfil().subscribe({
      next: (response: ApiResponse) => {
        this.perfil.set(response.usuario || response || {});
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar perfil:', error);
        this.errorMensaje.set('Error al cargar el perfil.');
      },
    });

    // Cargar casos
    this.casoService.getFeed(0, 20).subscribe({
      next: (response: ApiResponse) => {
        const casos = response.casos || response.content || response || [];
        this.casos.set(casos);

        // Calcular estadísticas basadas en los casos
        const enCustodia = casos.filter((c: ApiResponse) => c.estado === 'EN_CUSTODIA').length;
        const esperandoAtencion = casos.filter(
          (c: ApiResponse) => c.estado === 'ESPERANDO_ATENCION',
        ).length;
        const emergencias = casos.filter((c: ApiResponse) => c.urgencia === 'ALTA').length;

        this.resumen.set({
          enCustodia,
          esperandoAtencion,
          emergencias,
        });
        this.cargando.set(false);
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar casos:', error);
        this.errorMensaje.set('Error al cargar los casos.');
        this.cargando.set(false);
      },
    });
  }
}

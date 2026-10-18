import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { BitacoraService } from '../../core/services/bitacora.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { CasoService } from '../../core/services/caso.service';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

type VetSection = 'solicitudes' | 'pacientes' | 'historial' | 'informes' | 'mensajes' | 'perfil';

@Component({
  selector: 'app-panel-veterinaria',
  imports: [CommonModule, TitleCasePipe],
  templateUrl: './panel-veterinaria.component.html',
  styleUrl: './panel-veterinaria.component.css',
})
export class PanelVeterinariaComponent implements OnInit {
  private bitacoraService = inject(BitacoraService);
  private usuarioService = inject(UsuarioService);
  private casoService = inject(CasoService);

  activeSection = signal<VetSection>('solicitudes');
  solicitudes = signal<ApiResponse[]>([]);
  perfil = signal<ApiResponse>(null);
  cargando = signal(false);
  errorMensaje = signal('');

  ngOnInit(): void {
    this.cargarDatos();
  }

  setSection(section: VetSection): void {
    this.activeSection.set(section);
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    // Cargar perfil del usuario
    this.usuarioService.getMiPerfil().subscribe({
      next: (response: ApiResponse) => {
        this.perfil.set(response.usuario || response || {});
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar perfil:', error);
        this.errorMensaje.set('Error al cargar el perfil.');
      },
    });

    // Cargar solicitudes (casos)
    this.casoService.getFeed(0, 20).subscribe({
      next: (response: ApiResponse) => {
        const casos = response.casos || response.content || response || [];
        this.solicitudes.set(casos);
        this.cargando.set(false);
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar solicitudes:', error);
        this.errorMensaje.set('Error al cargar las solicitudes.');
        this.cargando.set(false);
      },
    });
  }
}

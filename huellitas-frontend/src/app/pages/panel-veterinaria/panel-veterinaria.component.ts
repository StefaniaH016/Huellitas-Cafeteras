import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { BitacoraService } from '../../core/services/bitacora.service';
import { CasoListItem, CasoService } from '../../core/services/caso.service';
import { PacienteResponseDTO } from '../../../core/models/dtos';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import { getPanelBadgeClass, getPanelBadgeLabel } from '../../core/utils/panel.utils';
import { Page, pageContent, pageTotal } from '../../core/utils/page.utils';
import { forkJoin } from 'rxjs';
import { CasoMiniaturaComponent } from '../../shared/components/caso-miniatura/caso-miniatura.component';

@Component({
  selector: 'app-panel-veterinaria',
  imports: [CommonModule, RouterLink, CasoMiniaturaComponent],
  templateUrl: './panel-veterinaria.component.html',
  styleUrl: './panel-veterinaria.component.css',
})
export class PanelVeterinariaComponent implements OnInit {
  private bitacoraService = inject(BitacoraService);
  private casoService = inject(CasoService);
  private usuarioService = inject(UsuarioService);
  private router = inject(Router);
  authService = inject(AuthService);

  // Stats
  veterinariaStats = signal({
    solicitudes: 0,
    pacientesActivos: 0,
    controlesProgramados: 0,
    altasEstesMes: 0,
  });
  veterinariaSolicitudes = signal<CasoListItem[]>([]);
  veterinariaPacientes = signal<PacienteResponseDTO[]>([]);
  altasRecientes = signal<PacienteResponseDTO[]>([]);
  nombreOrganizacion = signal('');
  descargandoPdf = signal<number | null>(null);
  errorPdf = signal('');

  cargando = signal(false);
  errorMensaje = signal('');

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.reiniciarEstadoCarga();
    forkJoin({
      perfil: this.usuarioService.getMiPerfil(),
      pacientes: this.bitacoraService.listarMisPacientes(),
    }).subscribe({
      next: ({ perfil, pacientes }) => this.procesarDatos(perfil, pacientes),
      error: (error: unknown) => this.manejarErrorCarga(error),
    });
  }

  private reiniciarEstadoCarga(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');
    this.veterinariaPacientes.set([]);
    this.veterinariaSolicitudes.set([]);
    this.altasRecientes.set([]);
    this.errorPdf.set('');
    this.veterinariaStats.set({
      solicitudes: 0,
      pacientesActivos: 0,
      controlesProgramados: 0,
      altasEstesMes: 0,
    });
  }

  private procesarDatos(perfil: UsuarioPerfil, pacientes: PacienteResponseDTO[]): void {
    this.nombreOrganizacion.set(perfil.organizacionNombre?.trim() || perfil.nombre);
    if (!perfil.municipioId) {
      this.errorMensaje.set('No se pudo determinar el municipio de tu organización.');
      this.cargando.set(false);
      return;
    }
    this.actualizarPacientes(pacientes);
    this.cargarSolicitudes(perfil.municipioId);
  }

  private actualizarPacientes(pacientes: PacienteResponseDTO[]): void {
    const pacientesActivos = pacientes.filter(
      (paciente) => !paciente.altaMedica && paciente.estado === 'EN_ATENCION',
    );
    const altasRecientes = pacientes
      .filter((paciente) => paciente.altaMedica)
      .sort((a, b) => (b.fechaAlta ?? '').localeCompare(a.fechaAlta ?? ''));
    const ahora = new Date();
    const altasEstesMes = altasRecientes.filter((paciente) => {
      if (!paciente.fechaAlta) {
        return false;
      }
      const fechaAlta = new Date(paciente.fechaAlta);
      return (
        !Number.isNaN(fechaAlta.getTime()) &&
        fechaAlta.getFullYear() === ahora.getFullYear() &&
        fechaAlta.getMonth() === ahora.getMonth()
      );
    }).length;
    this.veterinariaPacientes.set(pacientesActivos);
    this.altasRecientes.set(altasRecientes);
    this.veterinariaStats.set({
      solicitudes: 0,
      pacientesActivos: pacientesActivos.length,
      controlesProgramados: pacientesActivos.filter((paciente) => !!paciente.proximoControl).length,
      altasEstesMes,
    });
  }

  private cargarSolicitudes(municipioId: number): void {
    this.casoService
      .listarCasos({ municipioId, estado: 'REGISTRADO', page: 0, size: 10 })
      .subscribe({
        next: (solicitudes) => this.actualizarSolicitudes(solicitudes),
        error: (error: unknown) => this.manejarErrorSolicitudes(error),
      });
  }

  private actualizarSolicitudes(solicitudes: Page<CasoListItem> | CasoListItem[]): void {
    this.veterinariaSolicitudes.set(pageContent<CasoListItem>(solicitudes));
    this.veterinariaStats.update((stats) => ({
      ...stats,
      solicitudes: pageTotal(solicitudes),
    }));
    this.cargando.set(false);
  }

  private manejarErrorCarga(error: unknown): void {
    console.error('Error al cargar el panel veterinario:', error);
    this.errorMensaje.set('No fue posible cargar la información del panel veterinario.');
    this.cargando.set(false);
  }

  private manejarErrorSolicitudes(error: unknown): void {
    console.error('Error al cargar las solicitudes de atención:', error);
    this.errorMensaje.set('No fue posible cargar las solicitudes de atención.');
    this.cargando.set(false);
  }

  abrirBitacora(casoId: number): void {
    void this.router.navigate(['/caso-detalle', casoId], {
      queryParams: { tab: 'bitacora', formulario: 'entrada' },
    });
  }

  irAlCaso(casoId: number): void {
    void this.router.navigate(['/caso-detalle', casoId]);
  }

  descargarHistoria(casoId: number): void {
    if (this.descargandoPdf() !== null) {
      return;
    }
    this.errorPdf.set('');
    this.descargandoPdf.set(casoId);
    this.bitacoraService.descargarPdf(casoId).subscribe({
      next: (pdf) => {
        const url = URL.createObjectURL(pdf);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `historia-clinica-caso-${casoId}.pdf`;
        enlace.click();
        URL.revokeObjectURL(url);
        this.descargandoPdf.set(null);
      },
      error: (error: unknown) => {
        console.error('Error al descargar la historia clínica:', error);
        this.errorPdf.set('No fue posible descargar la historia clínica.');
        this.descargandoPdf.set(null);
      },
    });
  }

  getBadgeClass = getPanelBadgeClass;
  getBadgeLabel = getPanelBadgeLabel;
}

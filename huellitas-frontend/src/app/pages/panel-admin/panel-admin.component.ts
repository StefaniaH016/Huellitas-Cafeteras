import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CasoService } from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';

interface Caso {
  id: number;
  titulo?: string;
  descripcion: string;
  urgencia: string;
  municipio: string;
  barrio: string;
  foto?: string;
  reportadoHace?: string;
  estado?: string;
  nombre?: string;
  tipo?: string;
  solicitadoPor?: string;
  casoId?: number;
  proximoControl?: string;
  denunciadosPor?: number;
  motivo?: string;
}

@Component({
  selector: 'app-panel-admin',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-admin.component.html',
  styleUrl: './panel-admin.component.css',
})
export class PanelAdminComponent implements OnInit {
  private casoService = inject(CasoService);
  authService = inject(AuthService);

  // Stats
  adminStats = signal({ cuentasPorValidar: 0, contenidosDenunciados: 0, casosActivos: 0 });
  adminValidaciones = signal<Caso[]>([]);
  adminModeraccion = signal<Caso[]>([]);
  adminActiveTab = signal('validar');

  cargando = signal(false);
  errorMensaje = signal('');

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');
    // TODO: Load data from backend (phase 2)
    // For now, show empty state
    this.adminStats.set({ cuentasPorValidar: 0, contenidosDenunciados: 0, casosActivos: 0 });
    this.adminValidaciones.set([]);
    this.adminModeraccion.set([]);
    this.cargando.set(false);
  }

  setAdminTab(tab: string): void {
    this.adminActiveTab.set(tab);
  }

  scrollToPanel(): void {
    const element = document.getElementById('mi-panel');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
}

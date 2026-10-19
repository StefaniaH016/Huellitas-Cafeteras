import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
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
}

@Component({
  selector: 'app-panel-veterinaria',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './panel-veterinaria.component.html',
  styleUrl: './panel-veterinaria.component.css',
})
export class PanelVeterinariaComponent implements OnInit {
  private casoService = inject(CasoService);
  authService = inject(AuthService);

  // Stats
  veterinariaStats = signal({ solicitudes: 0, pacientesActivos: 0 });
  veterinariaSolicitudes = signal<Caso[]>([]);
  veterinariaPacientes = signal<Caso[]>([]);

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
    this.veterinariaStats.set({ solicitudes: 0, pacientesActivos: 0 });
    this.veterinariaSolicitudes.set([]);
    this.veterinariaPacientes.set([]);
    this.cargando.set(false);
  }

  getBadgeClass(urgencia: string): string {
    const urgenciaLower = (urgencia || '').toLowerCase();
    if (urgenciaLower === 'critica' || urgenciaLower === 'emergencia') {
      return 'badge-emergencia';
    }
    if (urgenciaLower === 'alta' || urgenciaLower === 'extraviado') {
      return 'badge-alta';
    }
    return 'badge-atencion';
  }

  getBadgeLabel(urgencia: string): string {
    const urgenciaLower = (urgencia || '').toLowerCase();
    if (urgenciaLower === 'critica' || urgenciaLower === 'emergencia') {
      return 'Emergencia';
    }
    if (urgenciaLower === 'alta' || urgenciaLower === 'extraviado') {
      return 'Extraviado';
    }
    return 'En atención';
  }

  scrollToPanel(): void {
    const element = document.getElementById('mi-panel');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
}

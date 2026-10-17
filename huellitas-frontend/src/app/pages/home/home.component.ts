import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CasoService } from '../../core/services/caso.service';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ApiResponse = any;

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private casoService = inject(CasoService);

  casos = signal<ApiResponse[]>([]);
  estadisticas = signal<{ total: number; resueltos: number; fundaciones: number }>({
    total: 0,
    resueltos: 0,
    fundaciones: 0,
  });
  cargando = signal(false);
  errorMensaje = signal('');

  ngOnInit(): void {
    this.cargarFeed();
  }

  cargarFeed(): void {
    this.cargando.set(true);
    this.errorMensaje.set('');

    this.casoService.getFeed(0, 10).subscribe({
      next: (response: ApiResponse) => {
        this.casos.set(response.casos || response.content || response || []);
        // Mapear datos de estadísticas desde la respuesta
        this.estadisticas.set({
          total: response.total || response.casos?.length || 0,
          resueltos: response.resueltos || 0,
          fundaciones: response.fundaciones || 0,
        });
        this.cargando.set(false);
      },
      error: (error: ApiResponse) => {
        console.error('Error al cargar feed de casos:', error);
        this.errorMensaje.set('Error al cargar los casos. Intente más tarde.');
        this.cargando.set(false);
      },
    });
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
}

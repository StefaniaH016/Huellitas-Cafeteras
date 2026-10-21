import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CasoService } from '../../core/services/caso.service';

interface CasoReporte {
  id: number;
  estado: string;
  urgencia: string;
  tipoCaso?: string;
  descripcion: string;
  municipio?: string;
  barrio?: string;
  fotoPrincipal?: string;
}

@Component({
  selector: 'app-mis-reportes',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mis-reportes.component.html',
  styleUrl: './mis-reportes.component.css',
})
export class MisReportesComponent implements OnInit {
  private casoService = inject(CasoService);

  reportes = signal<CasoReporte[]>([]);
  cargando = signal(false);
  error = signal('');
  eliminandoId = signal<number | null>(null);

  ngOnInit(): void {
    this.cargarReportes();
  }

  cargarReportes(): void {
    this.cargando.set(true);
    this.error.set('');
    this.casoService.getMisReportes().subscribe({
      next: (response: unknown): void => {
        const items = Array.isArray(response)
          ? response
          : (response as { content?: unknown[] } | null)?.content;
        this.reportes.set(Array.isArray(items) ? (items as CasoReporte[]) : []);
        this.cargando.set(false);
      },
      error: (err: unknown): void => {
        console.error('Error cargando mis reportes:', err);
        this.error.set('No fue posible cargar tus reportes. Intenta de nuevo.');
        this.cargando.set(false);
      },
    });
  }

  eliminar(caso: CasoReporte): void {
    if (
      caso.estado !== 'REGISTRADO' ||
      !globalThis.confirm('¿Eliminar este caso? Esta acción es permanente y no se puede deshacer.')
    ) {
      return;
    }

    this.eliminandoId.set(caso.id);
    this.error.set('');
    this.casoService.eliminarCaso(caso.id).subscribe({
      next: (): void => {
        this.reportes.update((items) => items.filter((item) => item.id !== caso.id));
        this.eliminandoId.set(null);
      },
      error: (err: unknown): void => {
        console.error('Error eliminando el caso:', err);
        this.error.set('No fue posible eliminar el caso. Intenta de nuevo.');
        this.eliminandoId.set(null);
      },
    });
  }
}

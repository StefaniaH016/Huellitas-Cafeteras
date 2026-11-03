import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import { ROLE_LABELS } from '../../core/utils/role.utils';
import { EstadoAccion, EstadoCarga } from '../../core/utils/request.utils';
import { UbicacionSelectorComponent } from '../../shared/components/ubicacion-selector/ubicacion-selector.component';

/** Mi cuenta (RF-04): datos de contacto y zona de interés, con GET/PUT /usuarios/me. */
@Component({
  selector: 'app-cuenta',
  imports: [FormsModule, UbicacionSelectorComponent],
  templateUrl: './cuenta.component.html',
})
export class CuentaComponent implements OnInit {
  private usuarioService = inject(UsuarioService);
  private authService = inject(AuthService);

  readonly carga = new EstadoCarga();
  readonly guardado = new EstadoAccion();
  readonly perfil = signal<UsuarioPerfil | null>(null);
  readonly roleLabels = ROLE_LABELS;

  nombre = '';
  telefono = '';
  zona = {
    departamentoId: null as number | null,
    municipioId: null as number | null,
    barrioId: null as number | null,
  };

  ngOnInit(): void {
    this.carga.ejecutar(this.usuarioService.getMiPerfil(), (perfil) => this.aplicar(perfil));
  }

  guardar(): void {
    if (!this.nombre.trim()) {
      return;
    }
    this.guardado.realizar(
      this.usuarioService.actualizarPerfil({
        nombre: this.nombre.trim(),
        telefono: this.telefono.trim() || undefined,
        barrioId: this.zona.barrioId,
      }),
      'Tus datos quedaron guardados.',
      (perfil) => this.aplicar(perfil),
      'No fue posible guardar tus datos. Revisa el teléfono e intenta de nuevo.',
    );
  }

  salir(): void {
    this.authService.logout();
  }

  private aplicar(perfil: UsuarioPerfil): void {
    this.perfil.set(perfil);
    this.nombre = perfil.nombre;
    this.telefono = perfil.telefono ?? '';
    this.zona = {
      departamentoId: perfil.departamentoId ?? null,
      municipioId: perfil.municipioId ?? null,
      barrioId: perfil.barrioId ?? null,
    };
  }
}

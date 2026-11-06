import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import {
  VerificacionResponse,
  VerificacionService,
} from '../../core/services/verificacion.service';
import { ROLE_LABELS } from '../../core/utils/role.utils';
import { EstadoAccion, EstadoCarga, getServerErrorMessage } from '../../core/utils/request.utils';
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
  private verificacionService = inject(VerificacionService);

  readonly carga = new EstadoCarga();
  readonly guardado = new EstadoAccion();
  readonly perfil = signal<UsuarioPerfil | null>(null);
  readonly roleLabels = ROLE_LABELS;
  readonly documentoRevision = signal<File | null>(null);
  readonly errorDocumento = signal('');
  readonly errorSolicitud = signal('');
  readonly mensajeSolicitud = signal('');
  readonly solicitudEnCurso = signal(false);
  readonly esInstitucional = computed(() => {
    const rol = this.perfil()?.rol;
    return rol === 'FUNDACION_REFUGIO' || rol === 'VETERINARIA';
  });
  readonly puedeSolicitarRevision = computed(
    () => this.esInstitucional() && this.perfil()?.estadoVerificacionOrganizacion !== 'VERIFICADA',
  );
  readonly requiereAsociacionOrganizacion = computed(
    () => this.esInstitucional() && !this.perfil()?.organizacionId,
  );

  nombre = '';
  telefono = '';
  correo = '';
  nombreEntidad = '';
  nitEntidad = '';
  municipioOperacion = '';
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

  seleccionarDocumento(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;
    this.errorDocumento.set('');
    this.mensajeSolicitud.set('');
    if (!archivo) {
      this.documentoRevision.set(null);
      return;
    }
    if (archivo.size > 5 * 1024 * 1024) {
      this.documentoRevision.set(null);
      this.errorDocumento.set('El documento no puede superar 5 MB.');
      input.value = '';
      return;
    }
    if (!/\.(pdf|jpe?g|png)$/i.test(archivo.name)) {
      this.documentoRevision.set(null);
      this.errorDocumento.set('Adjunta un archivo PDF, JPG o PNG.');
      input.value = '';
      return;
    }
    this.documentoRevision.set(archivo);
  }

  solicitarRevision(): void {
    const documento = this.validarDocumentoRevision();
    if (!documento || !this.validarOrganizacionRevision()) {
      return;
    }

    this.solicitudEnCurso.set(true);
    this.errorSolicitud.set('');
    this.mensajeSolicitud.set('');
    this.verificacionService
      .solicitarRevision(documento, this.datosOrganizacionRevision())
      .subscribe({
        next: (respuesta) => this.completarSolicitudRevision(respuesta),
        error: (error: unknown) => this.manejarErrorSolicitudRevision(error),
      });
  }

  private validarDocumentoRevision(): File | null {
    const documento = this.documentoRevision();
    if (!documento || this.solicitudEnCurso()) {
      this.errorSolicitud.set('Selecciona el documento de soporte actualizado.');
      return null;
    }
    return documento;
  }

  private validarOrganizacionRevision(): boolean {
    const datosCompletos =
      this.nombreEntidad.trim() && this.nitEntidad.trim() && this.municipioOperacion.trim();
    if (this.requiereAsociacionOrganizacion() && !datosCompletos) {
      this.errorSolicitud.set(
        'Completa el nombre, NIT y municipio de operación de tu organización.',
      );
      return false;
    }
    return true;
  }

  private datosOrganizacionRevision():
    { nombreEntidad: string; nit: string; municipioOperacion: string } | undefined {
    if (!this.requiereAsociacionOrganizacion()) {
      return undefined;
    }
    return {
      nombreEntidad: this.nombreEntidad.trim(),
      nit: this.nitEntidad.trim(),
      municipioOperacion: this.municipioOperacion.trim(),
    };
  }

  private completarSolicitudRevision(respuesta: VerificacionResponse): void {
    const debeActualizarPerfil = this.requiereAsociacionOrganizacion();
    this.perfil.update((perfil) =>
      perfil
        ? {
            ...perfil,
            verificado: respuesta.verificado,
            estadoVerificacionOrganizacion: respuesta.estado,
            aceptaCustodia: false,
            capacidadAlbergue: 0,
          }
        : perfil,
    );
    this.documentoRevision.set(null);
    this.solicitudEnCurso.set(false);
    this.mensajeSolicitud.set('La solicitud fue enviada al administrador para revisión.');
    if (debeActualizarPerfil) {
      this.recargarPerfilAsociado();
    }
  }

  private recargarPerfilAsociado(): void {
    this.usuarioService.getMiPerfil().subscribe({
      next: (perfil) => this.aplicar(perfil),
      error: (error: unknown) => {
        console.error('Error al actualizar el perfil tras asociar la organización:', error);
        this.errorSolicitud.set(
          'La solicitud se envió, pero no se pudo actualizar el perfil. Recarga la página.',
        );
      },
    });
  }

  private manejarErrorSolicitudRevision(error: unknown): void {
    console.error('Error al solicitar revisión de la organización:', error);
    this.solicitudEnCurso.set(false);
    this.errorSolicitud.set(
      getServerErrorMessage(error) ?? 'No fue posible enviar la solicitud. Intenta nuevamente.',
    );
  }

  private aplicar(perfil: UsuarioPerfil): void {
    this.perfil.set(perfil);
    this.nombre = perfil.nombre;
    this.correo = perfil.correo;
    this.telefono = perfil.telefono ?? '';
    this.zona = {
      departamentoId: perfil.departamentoId ?? null,
      municipioId: perfil.municipioId ?? null,
      barrioId: perfil.barrioId ?? null,
    };
  }
}

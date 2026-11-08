import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CasoService,
  CasoDetalle,
  CierreCasoDTO,
  ResultadoCierre,
  ResumenMedico,
} from '../../core/services/caso.service';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioPerfil, UsuarioService } from '../../core/services/usuario.service';
import {
  ESTADO_LABELS,
  RESULTADO_LABELS,
  TIPO_LABELS,
  esCasoAbierto,
  formatFecha as formatFechaDisplay,
  getUrgencyBadgeLabel,
  labelOf,
} from '../../core/utils/caso-display.utils';
import { getServerErrorMessage } from '../../core/utils/request.utils';
import { BitacoraService } from '../../core/services/bitacora.service';
import {
  CasoReportado,
  DecisionModeracion,
  ModeracionService,
} from '../../core/services/moderacion.service';
import { CasoSeguimientoComponent } from './caso-seguimiento/caso-seguimiento.component';
import { CasoBitacoraComponent } from './caso-bitacora/caso-bitacora.component';
import { CierreCasoDialogComponent } from './cierre-caso-dialog/cierre-caso-dialog.component';

interface Caso {
  id: string | number;
  titulo?: string;
  descripcion?: string;
  urgencia: string;
  tipo?: string;
  estado?: string;
  fechaReporte: string;
  especie?: string;
  tamanio?: string;
  edad?: string;
  color?: string;
  municipio?: string;
  barrio?: string;
  foto?: string;
  fotos: string[];
  fundacion?: string;
  veterinaria?: string;
  reportante?: string;
  resumenMedico?: ResumenMedico | null;
  autorId?: number;
  fundacionCustodioId?: number;
  aportes?: Record<string, unknown>[];
  historial?: Record<string, unknown>[];
}

interface AnimalResponse {
  id?: number;
  nombre?: string;
  especie?: string;
  raza?: string;
  color?: string;
  sexo?: string;
  edadAproximada?: string;
  senasParticulares?: string;
}

interface UbicacionResponse {
  departamentoId?: number;
  departamento?: string;
  municipioId?: number;
  municipio?: string;
  barrioId?: number;
  barrio?: string;
  direccionReferencia?: string;
}

interface FotoResponse {
  id?: number;
  url?: string;
  principal?: boolean;
}

function esResultadoCierre(resultado: string): resultado is ResultadoCierre {
  return ['REENCUENTRO', 'ENTREGADO_A_CUIDADOR', 'RECUPERADO', 'FALLECIDO'].includes(resultado);
}

interface CasoResponse {
  id?: string | number;
  tipoCaso?: string;
  estado?: string;
  urgencia?: string;
  descripcion?: string;
  animal?: AnimalResponse;
  ubicacion?: UbicacionResponse;
  fotografias?: FotoResponse[];
  fechaCreacion?: string;
  titulo?: string;
  especie?: string;
  tamanio?: string;
  edad?: string;
  color?: string;
  municipio?: string;
  barrio?: string;
  fechaReporte?: string;
  fundacion?: string;
  veterinaria?: string;
  reportanteNombre?: string;
  fundacionCustodioNombre?: string;
  veterinariaNombreResponsable?: string;
  resumenMedico?: ResumenMedico | null;
  autorId?: number;
  autorNombre?: string;
  fundacionCustodioId?: number;
  historial?: Record<string, unknown>[];
  aportes?: Record<string, unknown>[];
}

@Component({
  selector: 'app-caso-detalle',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CasoSeguimientoComponent,
    CasoBitacoraComponent,
    CierreCasoDialogComponent,
  ],
  templateUrl: './caso-detalle.component.html',
  styleUrls: ['./caso-detalle.component.css'],
})
export class CasoDetalleComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private casoService = inject(CasoService);
  private bitacoraService = inject(BitacoraService);
  private authService = inject(AuthService);
  private usuarioService = inject(UsuarioService);
  private moderacionService = inject(ModeracionService);
  private router = inject(Router);

  caso = signal<Caso | null>(null);
  perfil = signal<UsuarioPerfil | null>(null);
  cargando = signal(true);
  activeTab = signal<'informacion' | 'seguimiento' | 'bitacora'>('informacion');
  asumiendo = signal(false);
  tomandoAtencion = signal(false);
  sinBitacoraMedica = signal(false);
  observacionInicialMedica = '';
  formularioBitacora = signal<'entrada' | 'alta' | null>(null);
  accionEnCurso = signal(false);
  errorAccion = signal('');
  mensajeAccion = signal('');
  mostrandoArchivo = signal(false);
  motivoArchivo = '';
  mostrandoCierre = signal(false);
  motivoCierre: ResultadoCierre = 'RECUPERADO';
  observacionCierre = '';
  entregadoA = '';
  casoModeracion = signal<CasoReportado | null>(null);
  cargandoModeracion = signal(false);
  moderacionEnCurso = signal(false);
  errorModeracion = signal('');
  mensajeModeracion = signal('');
  motivoMarcaModeracion = '';
  motivoDecisionModeracion = 'Duplicado de otro reporte';
  marcacionModeracionLocal = signal(false);
  moderacionFinalizada = signal(false);
  fotos = signal<string[]>([]);
  selectedFotoIndex = signal<number>(0);
  fotoActual = computed(() => {
    const list = this.fotos();
    const idx = this.selectedFotoIndex();
    if (idx >= 0 && idx < list.length) {
      return list[idx];
    }
    return this.caso()?.foto || '';
  });
  resultadoCierre = computed(() => {
    const historial = this.caso()?.historial ?? [];
    const cierre = [...historial].reverse().find((item) => {
      const observacion = item['observacion'];
      return typeof observacion === 'string' && observacion.startsWith('Cierre de caso:');
    });
    const observacion = cierre?.['observacion'];
    if (typeof observacion !== 'string') {
      return null;
    }

    const resultado = /^Cierre de caso:\s*(REENCUENTRO|ENTREGADO_A_CUIDADOR|RECUPERADO|FALLECIDO)/i
      .exec(observacion)?.[1]
      ?.toUpperCase();
    return resultado && esResultadoCierre(resultado) ? resultado : null;
  });
  resultadoCierreLabel = computed(() => {
    const resultado = this.resultadoCierre();
    return resultado ? labelOf(RESULTADO_LABELS, resultado) : 'Resultado no disponible';
  });
  esFundacion = computed(() => this.authService.tieneRol('FUNDACION_REFUGIO'));
  esVeterinaria = computed(() => this.authService.tieneRol('VETERINARIA'));
  esAdministrador = computed(() => this.authService.tieneRol('ADMINISTRADOR'));
  puedeAsumirCustodia = computed(
    () =>
      this.esFundacion() &&
      this.perfilHabilitadoParaCustodia() &&
      this.casoDisponibleParaCustodia(),
  );
  puedeArchivarCaso = computed(
    () =>
      this.esAdministrador() &&
      ['REGISTRADO', 'EN_ATENCION', 'RESUELTO'].includes(this.caso()?.estado ?? ''),
  );
  esReportanteCaso = computed(() => {
    const perfil = this.perfil();
    const caso = this.caso();
    return (
      this.authService.tieneRol('CIUDADANO_VOLUNTARIO') &&
      perfil !== null &&
      caso !== null &&
      perfil.id === caso.autorId
    );
  });
  puedeCerrarCaso = computed(() => {
    const caso = this.caso();
    if (!caso || !['REGISTRADO', 'EN_ATENCION'].includes(caso.estado ?? '')) {
      return false;
    }
    if (this.esAdministrador()) {
      return true;
    }
    if (this.esReportanteCaso()) {
      return !caso.fundacionCustodioId;
    }

    const perfil = this.perfil();
    return (
      this.esFundacion() &&
      perfil?.estadoVerificacionOrganizacion === 'VERIFICADA' &&
      !!caso.fundacionCustodioId &&
      perfil.organizacionId === caso.fundacionCustodioId
    );
  });
  puedeConfirmarReencuentro = computed(
    () => this.esReportanteCaso() && this.caso()?.tipo === 'EXTRAVIADO',
  );
  puedeGestionarBitacora = computed(() => this.esVeterinariaResponsable());
  puedeDescargarBitacoraPdf = computed(() => {
    const perfil = this.perfil();
    const caso = this.caso();
    return (
      this.esAdministrador() ||
      (this.esVeterinaria() && perfil?.verificado === true) ||
      this.esReportanteCaso() ||
      (this.esFundacion() &&
        !!caso?.fundacionCustodioId &&
        perfil?.organizacionId === caso.fundacionCustodioId)
    );
  });

  private perfilHabilitadoParaCustodia(): boolean {
    const perfil = this.perfil();
    return (
      !!perfil &&
      perfil.estadoVerificacionOrganizacion === 'VERIFICADA' &&
      perfil.aceptaCustodia === true
    );
  }

  private casoDisponibleParaCustodia(): boolean {
    const caso = this.caso();
    return (
      !caso?.fundacionCustodioId &&
      (caso?.estado === 'REGISTRADO' || caso?.estado === 'EN_ATENCION')
    );
  }

  private esVeterinariaResponsable(): boolean {
    const caso = this.caso();
    const perfil = this.perfil();
    if (!this.esVeterinaria() || perfil?.verificado !== true) {
      return false;
    }
    return !caso?.resumenMedico || caso.resumenMedico.responsableId === perfil.id;
  }
  puedeTomarAtencion = computed(
    () =>
      this.puedeGestionarBitacora() &&
      this.sinBitacoraMedica() &&
      this.caso()?.estado === 'EN_ATENCION',
  );
  puedeAportarCaso = computed(() => {
    const caso = this.caso();
    const perfil = this.perfil();
    if (!caso || !perfil || !esCasoAbierto(caso.estado ?? '')) {
      return false;
    }

    return (
      (this.authService.tieneRol('CIUDADANO_VOLUNTARIO') &&
        (perfil.id !== caso.autorId || !caso.fundacionCustodioId)) ||
      (this.esFundacion() &&
        !!caso.fundacionCustodioId &&
        perfil.organizacionId === caso.fundacionCustodioId)
    );
  });
  puedeConsultarBitacora = computed(() => {
    const perfil = this.perfil();
    const caso = this.caso();
    return (
      this.esAdministrador() ||
      (this.esVeterinaria() && perfil?.verificado === true) ||
      this.esReportanteCaso() ||
      (this.esFundacion() &&
        !!caso?.fundacionCustodioId &&
        perfil?.organizacionId === caso.fundacionCustodioId)
    );
  });

  casoDet = computed(() => {
    const c = this.caso();
    if (!c) {
      return null;
    }
    return {
      id: Number(c.id),
      titulo: c.titulo,
      descripcion: c.descripcion,
      urgencia: c.urgencia,
      estado: c.estado || '',
      tipo: c.tipo,
      tipoCaso: c.tipo,
      municipio: c.municipio,
      barrio: c.barrio,
      fotografias: c.fotos,
      foto: c.foto,
      resumenMedico: c.resumenMedico,
      fundacionCustodioNombre: c.fundacion,
      veterinariaNombreResponsable: c.veterinaria,
      reportanteNombre: c.reportante,
      autorId: c.autorId,
      fundacionCustodioId: c.fundacionCustodioId,
      aportes: c.aportes ?? [],
      historial: c.historial ?? [],
    } as CasoDetalle;
  });

  ngOnInit(): void {
    this.activatedRoute.queryParamMap.subscribe((params) => {
      if (params.get('tab') === 'bitacora') {
        this.selectTab('bitacora');
      }
      const formulario = params.get('formulario');
      if (formulario === 'entrada' || formulario === 'alta') {
        this.formularioBitacora.set(formulario);
        this.selectTab('bitacora');
      }
    });
    this.usuarioService.getMiPerfil().subscribe({
      next: (perfil) => this.perfil.set(perfil),
      error: (error: unknown) => {
        console.error('Error loading current user profile:', error);
        this.errorAccion.set(
          'No fue posible verificar tu perfil. No se mostrarán acciones que requieren validar tu relación con el caso.',
        );
      },
    });
    this.activatedRoute.params.subscribe((params) => {
      const id = params['id'];
      if (id) {
        this.loadDetalle(id);
        if (this.esAdministrador()) {
          this.cargarModeracionCaso(id);
        }
      }
    });
  }

  cargarModeracionCaso(casoId: string | number): void {
    this.cargandoModeracion.set(true);
    this.errorModeracion.set('');
    this.moderacionService.getCasosReportados().subscribe({
      next: (casos) => {
        this.casoModeracion.set(
          casos.find((caso) => Number(caso.casoId) === Number(casoId)) ?? null,
        );
        this.marcacionModeracionLocal.set(false);
        this.moderacionFinalizada.set(false);
        this.cargandoModeracion.set(false);
      },
      error: (error: unknown) => {
        console.error('Error al cargar la moderación del caso:', error);
        this.errorModeracion.set(
          'No fue posible cargar el estado de moderación. Intenta nuevamente.',
        );
        this.cargandoModeracion.set(false);
      },
    });
  }

  resolverModeracion(decision: DecisionModeracion): void {
    const caso = this.caso();
    const motivo = this.motivoDecisionModeracion.trim();
    if (!caso || !this.esAdministrador() || this.moderacionEnCurso()) {
      return;
    }
    if (!this.casoModeracion() && !this.marcacionModeracionLocal()) {
      this.errorModeracion.set('Primero marca el caso para moderación.');
      return;
    }
    if (!motivo) {
      this.errorModeracion.set('Escribe el motivo de la decisión de moderación.');
      return;
    }

    const motivoMarcacion =
      this.motivoMarcaModeracion.trim() || this.casoModeracion()?.motivo.trim();
    const motivoCompleto = motivoMarcacion
      ? `Motivo de marcación: ${motivoMarcacion}. Decisión: ${motivo}`
      : motivo;
    this.guardarDecisionModeracion(caso.id, decision, motivoCompleto);
  }

  marcarParaModeracion(): void {
    const caso = this.caso();
    const motivo = this.motivoMarcaModeracion.trim();
    if (!caso || !this.esAdministrador() || this.casoModeracion() || this.moderacionEnCurso()) {
      return;
    }
    if (!motivo) {
      this.errorModeracion.set('Selecciona el motivo para marcar el caso para moderación.');
      return;
    }

    this.errorModeracion.set('');
    this.mensajeModeracion.set('');
    this.marcacionModeracionLocal.set(true);
    this.mensajeModeracion.set('Caso marcado para moderación. Selecciona la decisión final.');
  }

  private guardarDecisionModeracion(
    casoId: string | number,
    decision: DecisionModeracion,
    motivo: string,
  ): void {
    this.moderacionEnCurso.set(true);
    this.errorModeracion.set('');
    this.mensajeModeracion.set('');
    this.moderacionService.resolverModeracion(Number(casoId), { decision, motivo }).subscribe({
      next: () => {
        this.moderacionEnCurso.set(false);
        this.moderacionFinalizada.set(true);
        this.mensajeModeracion.set(
          decision === 'MANTENER'
            ? 'El caso se mantuvo publicado.'
            : 'El caso fue ocultado del feed.',
        );
        this.loadDetalle(casoId);
      },
      error: (error: unknown) => {
        console.error('Error al moderar el caso:', error);
        this.moderacionEnCurso.set(false);
        this.errorModeracion.set('No fue posible guardar la decisión. Intenta nuevamente.');
      },
    });
  }

  private formatEspecie(especie?: string): string {
    const esp = (especie || '').toUpperCase();
    const map: Record<string, string> = {
      PERRO: 'Perro',
      CANINO: 'Perro',
      GATO: 'Gato',
      FELINO: 'Gato',
      OTRO: 'Otro',
    };
    if (map[esp]) {
      return map[esp];
    }
    if (especie) {
      return especie.charAt(0).toUpperCase() + especie.slice(1).toLowerCase();
    }
    return 'Animal';
  }

  private formatColorYsenas(color?: string, senas?: string): string {
    const c = color?.trim() || '';
    const s = senas?.trim() || '';
    if (c && s) {
      return `${c} · ${s}`;
    }
    return c || s || 'N/A';
  }

  private formatUbicacion(ubicacion?: UbicacionResponse): { mun: string; bar: string } {
    const mun = ubicacion?.municipio || 'N/A';
    const bar = ubicacion?.barrio || 'N/A';
    const dir = ubicacion?.direccionReferencia ? ` (${ubicacion.direccionReferencia})` : '';
    return { mun, bar: `${bar}${dir}` };
  }

  private formatFecha(fechaCreacion?: string): string {
    if (!fechaCreacion) {
      return new Date().toLocaleDateString('es-CO');
    }
    try {
      return new Date(fechaCreacion).toLocaleDateString('es-CO');
    } catch {
      return fechaCreacion;
    }
  }

  formatearFecha(fecha?: string): string {
    return formatFechaDisplay(fecha);
  }

  private extractFotos(response: CasoResponse): string[] {
    const fotos = response.fotografias ?? [];

    // Ordenar y extraer URLs válidas en un solo flujo declarativo
    const urls = [...fotos]
      .sort((a, b) => Number(!!b.principal) - Number(!!a.principal))
      .map((f) => f?.url?.trim())
      .filter((url): url is string => Boolean(url));

    if (urls.length > 0) {
      return urls;
    }

    // Fallback si no hay fotos en el arreglo
    const respRecord = response as Record<string, unknown>;
    const single = respRecord['fotoPrincipal'] ?? respRecord['foto'];

    return typeof single === 'string' && single.trim() ? [single.trim()] : [];
  }

  private extractAnimalInfo(a?: AnimalResponse): {
    especie: string;
    tamanio: string;
    edad: string;
    color: string;
  } {
    if (!a) {
      return { especie: 'N/A', tamanio: 'Mediano', edad: 'N/A', color: 'N/A' };
    }
    const tamanio = a.raza ? a.raza : 'Mediano';
    const edad = a.edadAproximada ? a.edadAproximada : 'N/A';
    return {
      especie: this.formatEspecie(a.especie),
      tamanio,
      edad,
      color: this.formatColorYsenas(a.color, a.senasParticulares),
    };
  }

  private buildCasoFromResponse(
    response: CasoResponse,
    fallbackId: string | number,
    animalInfo: { especie: string; tamanio: string; edad: string; color: string },
    ubicacion: { mun: string; bar: string },
    fotos: string[],
  ): Caso {
    const desc = response.descripcion ? response.descripcion : '';
    const titulo = desc ? desc : response.titulo ? response.titulo : '';
    const idVal = response.id !== undefined && response.id !== null ? response.id : fallbackId;
    const fotoPrincipal = fotos.length > 0 ? fotos[0] : '';

    return this.createCasoObject(
      idVal,
      titulo,
      desc,
      response.urgencia,
      response.tipoCaso,
      response.estado,
      response.fechaCreacion,
      animalInfo,
      ubicacion,
      fotoPrincipal,
      fotos,
      response.fundacionCustodioNombre,
      response.veterinariaNombreResponsable,
      response.reportanteNombre,
    );
  }

  private createCasoObject(
    id: string | number,
    titulo: string,
    descripcion: string,
    urgencia: string | undefined,
    tipoCaso: string | undefined,
    estado: string | undefined,
    fechaCreacion: string | undefined,
    animalInfo: { especie: string; tamanio: string; edad: string; color: string },
    ubicacion: { mun: string; bar: string },
    fotoPrincipal: string,
    fotos: string[],
    fundacion: string | undefined,
    veterinaria: string | undefined,
    reportante: string | undefined,
  ): Caso {
    return {
      id,
      titulo,
      descripcion,
      urgencia: urgencia ? urgencia : 'MEDIA',
      tipo: tipoCaso,
      estado,
      fechaReporte: this.formatFecha(fechaCreacion),
      especie: animalInfo.especie,
      tamanio: animalInfo.tamanio,
      edad: animalInfo.edad,
      color: animalInfo.color,
      municipio: ubicacion.mun,
      barrio: ubicacion.bar,
      foto: fotoPrincipal,
      fotos,
      fundacion: fundacion ? fundacion : '',
      veterinaria: veterinaria ? veterinaria : '',
      reportante: reportante ? reportante : '',
      autorId: undefined,
      fundacionCustodioId: undefined,
      aportes: [],
      historial: [],
    };
  }

  private mapResponseToCaso(response: CasoResponse, fallbackId: string | number): Caso {
    const ai = this.extractAnimalInfo(response.animal);
    const u = this.formatUbicacion(response.ubicacion);
    const fotos = this.extractFotos(response);
    return this.buildCasoFromResponse(response, fallbackId, ai, u, fotos);
  }

  selectFoto(index: number): void {
    if (index >= 0 && index < this.fotos().length) {
      this.selectedFotoIndex.set(index);
    }
  }

  prevFoto(): void {
    const total = this.fotos().length;
    if (total <= 1) {
      return;
    }
    const current = this.selectedFotoIndex();
    this.selectedFotoIndex.set((current - 1 + total) % total);
  }

  nextFoto(): void {
    const total = this.fotos().length;
    if (total <= 1) {
      return;
    }
    const current = this.selectedFotoIndex();
    this.selectedFotoIndex.set((current + 1) % total);
  }

  onFotoError(): void {
    const current = this.selectedFotoIndex();
    const list = this.fotos();
    if (list.length > 0) {
      const updated = list.filter((_, idx) => idx !== current);
      this.fotos.set(updated);
      this.selectedFotoIndex.set(0);
      const c = this.caso();
      if (c) {
        this.caso.set({ ...c, foto: updated[0] || '', fotos: updated });
      }
    }
  }

  loadDetalle(id: string | number): void {
    this.cargando.set(true);
    this.casoService.getDetalle(Number(id)).subscribe({
      next: (response: CasoResponse): void => {
        const mapped = this.mapResponseToCaso(response, id);
        mapped.resumenMedico = response.resumenMedico;
        mapped.veterinaria =
          response.resumenMedico?.veterinariaNombre ??
          response.veterinariaNombreResponsable ??
          mapped.veterinaria;
        mapped.autorId = response.autorId;
        mapped.reportante = response.autorNombre ?? mapped.reportante;
        mapped.fundacionCustodioId = response.fundacionCustodioId;
        mapped.fundacion = response.fundacionCustodioNombre ?? '';
        mapped.aportes = response.aportes ?? [];
        mapped.historial = response.historial ?? [];
        this.caso.set(mapped);
        this.fotos.set(mapped.fotos);
        this.selectedFotoIndex.set(0);
        this.cargando.set(false);
      },
      error: (err: unknown): void => {
        console.error('Error loading case detail:', err);
        this.cargando.set(false);
      },
    });
  }

  goBack(): void {
    void this.router.navigate(['/casos']);
  }

  selectTab(tab: 'informacion' | 'seguimiento' | 'bitacora'): void {
    this.activeTab.set(tab);
  }

  iniciarCierre(): void {
    if (!this.puedeCerrarCaso()) {
      return;
    }
    this.motivoCierre = this.puedeConfirmarReencuentro() ? 'REENCUENTRO' : 'RECUPERADO';
    this.observacionCierre = '';
    this.entregadoA = '';
    this.errorAccion.set('');
    this.mostrandoCierre.set(true);
  }

  cancelarCierre(): void {
    if (!this.accionEnCurso()) {
      this.mostrandoCierre.set(false);
    }
  }

  cerrarCaso(): void {
    const caso = this.caso();
    if (!this.validarCierre(caso)) {
      return;
    }

    const dto = this.crearCierreDTO(caso);
    this.accionEnCurso.set(true);
    this.errorAccion.set('');
    this.mensajeAccion.set('');
    this.casoService.cerrarCaso(Number(caso.id), dto).subscribe({
      next: () => this.finalizarCierre(caso),
      error: (error: unknown) => this.manejarErrorCierre(error),
    });
  }

  private validarCierre(caso: Caso | null): caso is Caso {
    if (!caso || !this.puedeCerrarCaso() || this.accionEnCurso()) {
      return false;
    }
    if (this.motivoCierre === 'REENCUENTRO' && !this.puedeConfirmarReencuentro()) {
      return false;
    }
    if (!caso.fundacionCustodioId && this.motivoCierre !== 'ENTREGADO_A_CUIDADOR') {
      return true;
    }
    if (this.entregadoA.trim()) {
      return true;
    }

    this.errorAccion.set(
      this.motivoCierre === 'ENTREGADO_A_CUIDADOR'
        ? 'Indica a quién fue entregado el animal.'
        : 'Indica la persona o familia responsable del cierre.',
    );
    return false;
  }

  private crearCierreDTO(caso: Caso): CierreCasoDTO {
    return {
      motivoCierre: this.motivoCierre,
      observacion: this.observacionCierre.trim() || undefined,
      entregadoA:
        this.motivoCierre === 'ENTREGADO_A_CUIDADOR' || caso.fundacionCustodioId
          ? this.entregadoA.trim() || undefined
          : undefined,
    };
  }

  private finalizarCierre(caso: Caso): void {
    this.accionEnCurso.set(false);
    this.mostrandoCierre.set(false);
    this.mensajeAccion.set(
      this.motivoCierre === 'REENCUENTRO'
        ? 'El reencuentro quedó confirmado y el caso fue cerrado.'
        : 'El caso fue cerrado correctamente.',
    );
    this.loadDetalle(caso.id);
  }

  private manejarErrorCierre(error: unknown): void {
    console.error('Error al cerrar el caso:', error);
    this.accionEnCurso.set(false);
    this.errorAccion.set(
      getServerErrorMessage(error) ?? 'No fue posible cerrar el caso. Intenta de nuevo.',
    );
  }

  abrirFormularioBitacora(formulario: 'entrada' | 'alta'): void {
    this.formularioBitacora.set(formulario);
    this.selectTab('bitacora');
  }

  tomarAtencionMedica(): void {
    const caso = this.caso();
    if (!caso || !this.puedeTomarAtencion() || this.tomandoAtencion()) {
      return;
    }

    this.tomandoAtencion.set(true);
    this.errorAccion.set('');
    this.mensajeAccion.set('');
    this.bitacoraService
      .abrirBitacora(Number(caso.id), this.observacionInicialMedica.trim() || undefined)
      .subscribe({
        next: () => {
          this.tomandoAtencion.set(false);
          this.sinBitacoraMedica.set(false);
          this.observacionInicialMedica = '';
          this.mensajeAccion.set('La atención médica quedó a tu cargo.');
          this.activeTab.set('bitacora');
          this.loadDetalle(caso.id);
        },
        error: (error: unknown) => {
          console.error('Error al tomar atención médica:', error);
          this.tomandoAtencion.set(false);
          this.errorAccion.set(
            getServerErrorMessage(error) ??
              'No fue posible tomar la atención médica. Intenta de nuevo.',
          );
        },
      });
  }

  asumirCustodia(): void {
    const caso = this.caso();
    if (!caso || !this.puedeAsumirCustodia() || this.asumiendo()) {
      return;
    }

    this.asumiendo.set(true);
    this.errorAccion.set('');
    this.mensajeAccion.set('');
    this.casoService.gestionarCustodia(Number(caso.id), { accion: 'ASUMIR' }).subscribe({
      next: () => {
        this.asumiendo.set(false);
        this.mensajeAccion.set('La custodia quedó a cargo de tu fundación.');
        this.loadDetalle(caso.id);
      },
      error: (err: unknown) => {
        this.asumiendo.set(false);
        console.error('Error al asumir custodia:', err);
        this.errorAccion.set(
          getServerErrorMessage(err) ??
            'No fue posible asumir la custodia. Verifica que tu fundación esté habilitada y tenga cupo.',
        );
      },
    });
  }

  archivarCaso(): void {
    const caso = this.caso();
    const motivo = this.motivoArchivo.trim();
    if (!caso || !this.puedeArchivarCaso() || !motivo || this.accionEnCurso()) {
      return;
    }
    this.accionEnCurso.set(true);
    this.errorAccion.set('');
    this.mensajeAccion.set('');
    this.casoService.archivarCaso(Number(caso.id), { motivo }).subscribe({
      next: () => {
        this.accionEnCurso.set(false);
        this.mostrandoArchivo.set(false);
        this.mensajeAccion.set('El caso fue archivado y quedó en solo lectura.');
        this.loadDetalle(caso.id);
      },
      error: (error: unknown) => {
        console.error('Error al archivar el caso:', error);
        this.accionEnCurso.set(false);
        this.errorAccion.set('No fue posible archivar el caso. Intenta de nuevo.');
      },
    });
  }

  getBadgeClass(urgencia?: string): string {
    switch (urgencia?.toUpperCase()) {
      case 'CRITICA':
        return 'badge-emergencia';
      case 'ALTA':
        return 'badge-alta';
      case 'MEDIA':
        return 'badge-atencion';
      case 'BAJA':
        return 'badge-media';
      default:
        return 'badge-atencion';
    }
  }

  getBadgeLabel(urgencia?: string): string {
    return getUrgencyBadgeLabel(urgencia || 'MEDIA');
  }

  getEstadoLabel(estado?: string): string {
    return estado ? labelOf(ESTADO_LABELS, estado) : '';
  }

  getEstadoBadgeClass(estado?: string): string {
    switch (estado?.toUpperCase()) {
      case 'REGISTRADO':
        return 'badge-registrado';
      case 'EN_ATENCION':
        return 'badge-atencion';
      case 'RESUELTO':
        return 'badge-resuelto';
      case 'ARCHIVADO':
        return 'badge-archivado';
      default:
        return 'badge-atencion';
    }
  }

  getTipoLabel(tipo?: string): string {
    return tipo ? labelOf(TIPO_LABELS, tipo) : '';
  }
}

package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.*;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.AporteMapper;
import co.edu.uniquindio.huellitas.mapper.CasoMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.CasoService;
import co.edu.uniquindio.huellitas.service.CloudinaryService;
import co.edu.uniquindio.huellitas.service.EmailService;
import co.edu.uniquindio.huellitas.service.validator.EstadoCasoValidator;
import co.edu.uniquindio.huellitas.specification.CasoSpecification;
import co.edu.uniquindio.huellitas.worker.FiltroWorker;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class CasoServiceImpl implements CasoService {

    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BarrioRepository barrioRepository;
    private final AnimalRepository animalRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final CierreCasoRepository cierreCasoRepository;
    private final NotificacionRepository notificacionRepository;
    private final CloudinaryService cloudinaryService;
    private final CasoMapper casoMapper;
    private final AporteMapper aporteMapper;
    private final NotificacionWorker notificacionWorker;
    private final FiltroWorker filtroWorker;
    private final EmailService emailService;
    private final EstadoCasoValidator estadoCasoValidator;
    private final BitacoraMedicaRepository bitacoraMedicaRepository;

    @Autowired
    public CasoServiceImpl(
            CasoRepository casoRepository,
            UsuarioRepository usuarioRepository,
            BarrioRepository barrioRepository,
            AnimalRepository animalRepository,
            HistorialEstadoRepository historialEstadoRepository,
            SeguimientoRepository seguimientoRepository,
            CierreCasoRepository cierreCasoRepository,
            NotificacionRepository notificacionRepository,
            CloudinaryService cloudinaryService,
            CasoMapper casoMapper,
            AporteMapper aporteMapper,
            NotificacionWorker notificacionWorker,
            FiltroWorker filtroWorker,
            EmailService emailService,
            EstadoCasoValidator estadoCasoValidator,
            BitacoraMedicaRepository bitacoraMedicaRepository
    ) {
        this.casoRepository = casoRepository;
        this.usuarioRepository = usuarioRepository;
        this.barrioRepository = barrioRepository;
        this.animalRepository = animalRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.cierreCasoRepository = cierreCasoRepository;
        this.notificacionRepository = notificacionRepository;
        this.cloudinaryService = cloudinaryService;
        this.casoMapper = casoMapper;
        this.aporteMapper = aporteMapper;
        this.notificacionWorker = notificacionWorker;
        this.filtroWorker = filtroWorker;
        this.emailService = emailService;
        this.estadoCasoValidator = estadoCasoValidator;
        this.bitacoraMedicaRepository = bitacoraMedicaRepository;
    }

    // Constructor de compatibilidad para pruebas unitarias existentes
    public CasoServiceImpl(
            CasoRepository casoRepository,
            UsuarioRepository usuarioRepository,
            BarrioRepository barrioRepository,
            AnimalRepository animalRepository,
            HistorialEstadoRepository historialEstadoRepository,
            SeguimientoRepository seguimientoRepository,
            CierreCasoRepository cierreCasoRepository,
            NotificacionRepository notificacionRepository,
            CloudinaryService cloudinaryService,
            CasoMapper casoMapper,
            AporteMapper aporteMapper,
            NotificacionWorker notificacionWorker,
            FiltroWorker filtroWorker,
            EmailService emailService
    ) {
        this(casoRepository, usuarioRepository, barrioRepository, animalRepository,
                historialEstadoRepository, seguimientoRepository, cierreCasoRepository,
                notificacionRepository, cloudinaryService, casoMapper, aporteMapper,
                notificacionWorker, filtroWorker, emailService, new EstadoCasoValidator(), null);
    }

    @Override
    public CasoResponseDTO crearCaso(CrearReporteDTO dto, String correoUsuario) {
        Usuario reportante = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));

        Barrio barrio = barrioRepository.findById(dto.barrioId())
                .orElseThrow(() -> new ResourceNotFoundException("Barrio", dto.barrioId()));

        Animal animal = casoMapper.toAnimalEntity(dto.animal(), reportante);
        animal = animalRepository.save(animal);

        Instant now = Instant.now();
        Caso caso = Caso.builder()
                .tipoCaso(dto.tipoCaso())
                .descripcion(dto.descripcion())
                .urgencia(dto.urgencia())
                .estado(EstadoCaso.REGISTRADO)
                .direccionReferencia(dto.direccionReferencia())
                .fechaRegistro(now)
                .fechaUltimaActividad(now)
                .visible(true)
                .reportante(reportante)
                .animal(animal)
                .barrio(barrio)
                .build();
        caso = casoRepository.save(caso);

        HistorialEstado historial = HistorialEstado.builder()
                .estadoAnterior(EstadoCaso.REGISTRADO)
                .estadoNuevo(EstadoCaso.REGISTRADO)
                .observacion("Registro inicial del caso")
                .fecha(now)
                .caso(caso)
                .usuario(reportante)
                .build();
        historialEstadoRepository.save(historial);

        // W-01: notificar al reportante si el caso es urgente
        notificacionWorker.notificarCasoUrgente(caso);

        // 13.6: Notificar emergencia a rescuistas del municipio
        try {
            notificacionWorker.notificarEmergenciaARescuatistas(caso);
        } catch (Exception e) {
            log.warn("Error al notificar emergencias a rescuistas: {}", e.getMessage());
        }

        return casoMapper.toResponseDTO(caso);
    }

    @Override
    public CasoResponseDTO actualizarReporte(Long id, CrearReporteDTO dto, String correoUsuario) {
        Caso caso = obtenerCasoEditable(id, correoUsuario);
        Barrio barrio = barrioRepository.findById(dto.barrioId())
                .orElseThrow(() -> new ResourceNotFoundException("Barrio", dto.barrioId()));

        AnimalReporteDTO animalDto = dto.animal();
        Animal animal = caso.getAnimal();
        animal.setNombre(animalDto.nombre());
        animal.setEspecie(animalDto.especie());
        animal.setRaza(animalDto.raza());
        animal.setColor(animalDto.color());
        animal.setSexo(animalDto.sexo());
        animal.setEdadAproximada(animalDto.edadAproximada());
        animal.setSenasParticulares(animalDto.senasParticulares());
        animalRepository.save(animal);

        caso.setTipoCaso(dto.tipoCaso());
        caso.setDescripcion(dto.descripcion());
        caso.setUrgencia(dto.urgencia());
        caso.setDireccionReferencia(dto.direccionReferencia());
        caso.setBarrio(barrio);
        caso.setFechaUltimaActividad(Instant.now());
        caso = casoRepository.save(caso);

        return casoMapper.toResponseDTO(caso);
    }

    @Override
    public void eliminarFoto(Long casoId, Long fotoId, String correoUsuario) {
        Caso caso = obtenerCasoEditable(casoId, correoUsuario);
        Foto foto = caso.getFotos().stream()
                .filter(item -> item.getId().equals(fotoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Foto", fotoId));

        if (caso.getFotos().size() <= 1) {
            throw new ConflictException("El caso debe conservar al menos una foto.");
        }

        cloudinaryService.eliminarFotos(List.of(foto));
        if (foto.isPrincipal()) {
            caso.getFotos().stream()
                    .filter(item -> !item.getId().equals(fotoId))
                    .findFirst()
                    .ifPresent(item -> item.setPrincipal(true));
        }
        caso.getFotos().remove(foto);
    }

    @Override
    public void eliminarCaso(Long id, String correoUsuario) {
        Caso caso = obtenerCasoEditable(id, correoUsuario);
        if (casoRepository.existsByCasoOriginalId(id)) {
            throw new ConflictException("No se puede eliminar el caso porque tiene casos relacionados.");
        }

        Animal animal = caso.getAnimal();
        boolean animalCompartido = casoRepository.existsByAnimalIdAndIdNot(animal.getId(), id);
        cloudinaryService.eliminarFotos(caso.getFotos());
        notificacionRepository.desvincularCaso(id);
        historialEstadoRepository.deleteByCasoId(id);
        casoRepository.delete(caso);
        casoRepository.flush();
        if (!animalCompartido) {
            animalRepository.delete(animal);
        }
    }

    private Caso obtenerCasoEditable(Long id, String correoUsuario) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        String correoNormalizado = correoUsuario.trim().toLowerCase();
        if (caso.getReportante() == null
                || !correoNormalizado.equals(caso.getReportante().getCorreo().trim().toLowerCase())) {
            throw new AccessDeniedException("Solo el reportante puede modificar o eliminar este caso.");
        }
        if (caso.getEstado() != EstadoCaso.REGISTRADO) {
            throw new ConflictException("Solo se pueden modificar o eliminar casos en estado REGISTRADO.");
        }
        return caso;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> listarCasos(FiltrosCasoDTO filtros, Pageable pageable) {
        // W-02: delegar filtrado al FiltroWorker
        return filtroWorker.filtrar(filtros, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CasoDetalleDTO obtenerDetalle(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        AnimalDetalleDTO animalDTO = casoMapper.toAnimalDetalleDTO(caso.getAnimal());
        UbicacionDetalleDTO ubicacionDTO = casoMapper.toUbicacionDetalleDTO(caso);
        List<FotoDetalleDTO> fotosDTO = casoMapper.toFotoDetalleDTOList(caso.getFotos());

        List<HistorialEstadoDTO> historialDTO = casoMapper.toHistorialEstadoDTOList(
                historialEstadoRepository.findByCasoIdOrderByFechaAsc(id)
        );

        List<AporteDetalleDTO> aportesDTO = aporteMapper.toDetalleDTOList(
                seguimientoRepository.findByCasoIdAndVisibleTrueOrderByFechaAsc(id)
        );

        ResumenMedicoDTO resumenMedicoDTO = casoMapper.toResumenMedicoDTO(caso.getBitacoraMedica());

        return casoMapper.toCasoDetalleDTO(caso, animalDTO, ubicacionDTO, fotosDTO, historialDTO, aportesDTO, resumenMedicoDTO);
    }

    @Override
    public CasoResponseDTO actualizarEstado(Long id, ActualizarEstadoDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        Usuario usuario = obtenerUsuario(correoUsuario);
        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            Fundacion fundacion = obtenerFundacionAprobada(usuario);
            if (caso.getFundacionCustodio() == null
                    || !Objects.equals(caso.getFundacionCustodio().getId(), fundacion.getId())) {
                throw new AccessDeniedException(
                        "Solo la fundación verificada que tiene el caso en custodia puede actualizar su seguimiento.");
            }
        }

        // Subtarea 16.1: Validar transición de estado
        estadoCasoValidator.validarTransicion(caso.getEstado(), dto.estado());

        EstadoCaso anterior = caso.getEstado();
        EstadoCaso nuevo = dto.estado();

        Instant now = Instant.now();
        caso.setEstado(nuevo);
        caso.setFechaUltimaActividad(now);

        caso = casoRepository.save(caso);

        HistorialEstado historial = HistorialEstado.builder()
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .observacion(dto.observacion() != null ? dto.observacion() : "Actualización de estado a " + nuevo)
                .fecha(now)
                .caso(caso)
                .usuario(usuario)
                .build();
        historialEstadoRepository.save(historial);

        Seguimiento seguimiento = Seguimiento.builder()
                .tipo(TipoAporte.CAMBIO_ESTADO)
                .comentario("Estado actualizado de " + anterior + " a " + nuevo + (dto.observacion() != null ? ". " + dto.observacion() : ""))
                .fecha(now)
                .caso(caso)
                .autor(usuario)
                .visible(true)
                .build();
        seguimientoRepository.save(seguimiento);

        // W-01: notificar cambio de estado
        notificacionWorker.notificarCambioEstado(caso, anterior, nuevo, usuario);

        return casoMapper.toResponseDTO(caso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoDTO> obtenerHistorial(Long id) {
        if (!casoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Caso", id);
        }

        return casoMapper.toHistorialEstadoDTOList(historialEstadoRepository.findByCasoIdOrderByFechaAsc(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> misReportes(String correoUsuario, Pageable pageable) {
        return casoRepository.findByReportanteCorreoOrderByFechaRegistroDesc(correoUsuario.trim().toLowerCase(), pageable)
                .map(casoMapper::toListItemDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> misReportes(String correoUsuario, EstadoCaso estado, Pageable pageable) {
        if (estado == null) {
            // Si no se proporciona estado, conservar comportamiento original
            return misReportes(correoUsuario, pageable);
        }
        return casoRepository.findByReportanteCorreoAndEstadoOrderByFechaRegistroDesc(
                correoUsuario.trim().toLowerCase(), estado, pageable)
                .map(casoMapper::toListItemDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> obtenerHistorialCasos(EstadoCaso estado, Pageable pageable) {
        List<EstadoCaso> estadosHistorial;
        
        if (estado == null) {
            // Por defecto, mostrar RESUELTO y ARCHIVADO
            estadosHistorial = List.of(EstadoCaso.RESUELTO, EstadoCaso.ARCHIVADO);
        } else if (estado == EstadoCaso.RESUELTO || estado == EstadoCaso.ARCHIVADO) {
            estadosHistorial = List.of(estado);
        } else {
            // Solo permitir RESUELTO y ARCHIVADO en el historial
            throw new IllegalArgumentException("El historial solo admite casos RESUELTO o ARCHIVADO");
        }

        return casoRepository.findByEstadoInOrderByFechaArchivadoDesc(estadosHistorial, pageable)
                .map(casoMapper::toListItemDTO);
    }

    @Override
    public CasoResponseDTO cerrarCaso(Long id, CierreCasoRequestDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        Usuario usuario = obtenerUsuario(correoUsuario);

        // Subtarea 16.1: Validar transición de estado a RESUELTO
        estadoCasoValidator.validarTransicion(caso.getEstado(), EstadoCaso.RESUELTO);

        // Validar permisos generales de cierre
        validarPermisoCierre(caso, usuario);

        // Subtarea 16.2.A: REENCUENTRO solo para EXTRAVIADO y solo el reportante
        if (dto.motivoCierre() == ResultadoCierre.REENCUENTRO) {
            if (caso.getTipoCaso() != TipoCaso.EXTRAVIADO) {
                throw new ConflictException("El resultado REENCUENTRO solo aplica para casos de tipo EXTRAVIADO.");
            }
            boolean esReportante = caso.getReportante() != null
                    && Objects.equals(caso.getReportante().getId(), usuario.getId());
            if (!esReportante) {
                throw new AccessDeniedException("Solo el ciudadano reportante del caso puede registrar el cierre por REENCUENTRO.");
            }
        }

        // Subtarea 16.2.B: entregadoA si hay custodia
        if (caso.getFundacionCustodio() != null) {
            if (dto.entregadoA() == null || dto.entregadoA().trim().isEmpty()) {
                throw new ConflictException("El campo 'entregadoA' es obligatorio para casos que cuentan con fundación custodia.");
            }
        }

        // Subtarea 16.2.C: alta médica obligatoria si el caso cuenta con bitácora médica
        BitacoraMedica bitacora = caso.getBitacoraMedica();
        if (bitacora == null && bitacoraMedicaRepository != null) {
            bitacora = bitacoraMedicaRepository.findByCasoId(caso.getId()).orElse(null);
        }
        if (bitacora != null && !bitacora.isAltaMedica()) {
            throw new ConflictException("No se puede cerrar el caso: la bitácora médica asociada requiere alta médica previa emitida por un profesional veterinario.");
        }

        EstadoCaso anterior = caso.getEstado();
        Instant now = Instant.now();

        CierreCaso cierre = CierreCaso.builder()
                .resultado(dto.motivoCierre())
                .observaciones(dto.observacion())
                .entregadoA(dto.entregadoA())
                .fecha(now)
                .caso(caso)
                .confirmador(usuario)
                .build();
        cierre = cierreCasoRepository.save(cierre);

        caso.setEstado(EstadoCaso.RESUELTO);
        caso.setFechaUltimaActividad(now);
        caso.setCierreCaso(cierre);
        caso = casoRepository.save(caso);

        HistorialEstado historial = HistorialEstado.builder()
                .estadoAnterior(anterior)
                .estadoNuevo(EstadoCaso.RESUELTO)
                .observacion("Cierre de caso: " + dto.motivoCierre() + (dto.observacion() != null ? ". " + dto.observacion() : ""))
                .fecha(now)
                .caso(caso)
                .usuario(usuario)
                .build();
        historialEstadoRepository.save(historial);

        Seguimiento seguimiento = Seguimiento.builder()
                .tipo(TipoAporte.ACTUALIZACION)
                .comentario("Caso cerrado con resultado: " + dto.motivoCierre() + (dto.observacion() != null ? ". " + dto.observacion() : ""))
                .fecha(now)
                .caso(caso)
                .autor(usuario)
                .visible(true)
                .build();
        seguimientoRepository.save(seguimiento);

        // Subtarea 16.4: Notificar cierre de caso (W-01)
        notificacionWorker.notificarCierreCaso(caso, dto.motivoCierre().name());

        // Subtarea 16.4: Enviar correo de cierre al reportante
        try {
            if (caso.getReportante() != null && caso.getReportante().getCorreo() != null) {
                emailService.enviarCorreoCierre(caso, caso.getReportante().getCorreo(), dto.motivoCierre().name());
            }
        } catch (Exception e) {
            log.warn("Error al enviar correo de cierre: {}", e.getMessage());
        }

        return casoMapper.toResponseDTO(caso);
    }

    @Override
    public CasoResponseDTO archivarCaso(Long id, ArchivarCasoDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        Usuario usuario = obtenerUsuario(correoUsuario);
        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new AccessDeniedException("Solo un administrador puede archivar un caso.");
        }

        // Subtarea 16.1: Validar transición de estado a ARCHIVADO
        estadoCasoValidator.validarTransicion(caso.getEstado(), EstadoCaso.ARCHIVADO);

        EstadoCaso anterior = caso.getEstado();
        Instant now = Instant.now();

        caso.setEstado(EstadoCaso.ARCHIVADO);
        caso.setFechaArchivado(now);
        caso.setFechaUltimaActividad(now);
        caso.setMotivoModeracion(dto.motivo());
        caso = casoRepository.save(caso);

        HistorialEstado historial = HistorialEstado.builder()
                .estadoAnterior(anterior)
                .estadoNuevo(EstadoCaso.ARCHIVADO)
                .observacion("Caso archivado. Motivo: " + dto.motivo())
                .fecha(now)
                .caso(caso)
                .usuario(usuario)
                .build();
        historialEstadoRepository.save(historial);

        Seguimiento seguimiento = Seguimiento.builder()
                .tipo(TipoAporte.ACTUALIZACION)
                .comentario("Caso archivado: " + dto.motivo())
                .fecha(now)
                .caso(caso)
                .autor(usuario)
                .visible(true)
                .build();
        seguimientoRepository.save(seguimiento);

        notificacionWorker.notificarArchivadoCaso(caso, dto.motivo());

        return casoMapper.toResponseDTO(caso);
    }

    private Usuario obtenerUsuario(String correoUsuario) {
        return usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con correo: " + correoUsuario));
    }

    private Fundacion obtenerFundacionAprobada(Usuario usuario) {
        if (usuario.getRol() != Rol.FUNDACION_REFUGIO) {
            throw new AccessDeniedException("Se requiere una cuenta de fundación verificada.");
        }
        if (usuario.getOrganizacion() == null && usuario.isVerificado()) {
            Fundacion autoFundacion = Fundacion.builder()
                    .nombre(usuario.getNombre())
                    .nit("NIT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                    .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                    .aceptaCustodia(true)
                    .capacidadAlbergue(50)
                    .municipio(usuario.getMunicipioInteres())
                    .fechaVerificacion(Instant.now())
                    .build();
            usuario.setOrganizacion(autoFundacion);
            usuario = usuarioRepository.save(usuario);
        }
        if (!(usuario.getOrganizacion() instanceof Fundacion fundacion)
                || fundacion.getEstadoVerificacion() != EstadoVerificacion.VERIFICADA) {
            throw new AccessDeniedException("Se requiere una cuenta de fundación verificada.");
        }
        return fundacion;
    }

    private void validarPermisoCierre(Caso caso, Usuario usuario) {
        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            return;
        }

        if (usuario.getRol() == Rol.CIUDADANO_VOLUNTARIO) {
            boolean esReportante = caso.getReportante() != null
                    && Objects.equals(caso.getReportante().getId(), usuario.getId());
            if (!esReportante || caso.getFundacionCustodio() != null) {
                throw new AccessDeniedException(
                        "Solo el reportante puede cerrar un caso que no tenga fundación custodio.");
            }
            return;
        }

        Fundacion fundacion = obtenerFundacionAprobada(usuario);
        boolean esCustodiaActual = caso.getFundacionCustodio() != null
                && Objects.equals(caso.getFundacionCustodio().getId(), fundacion.getId());
        if (!esCustodiaActual) {
            throw new AccessDeniedException(
                    "La fundación solo puede cerrar casos que tenga bajo su custodia.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> getFeed(Pageable pageable) {
        List<Urgencia> urgentes = List.of(Urgencia.ALTA, Urgencia.CRITICA);
        return casoRepository.findByUrgenciaInAndVisibleTrueOrderByFechaRegistroDesc(urgentes, pageable)
                .map(casoMapper::toListItemDTO);
    }

    // 13.3: Gestión de custodia (asumir o liberar)
    @Override
    public CasoResponseDTO gestionarCustodia(Long casoId, CustodiaDTO dto, String correoUsuario) {
        // Obtener el usuario autenticado (fundación)
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));

        // Si es una fundación verificada sin entidad Organización persistida, auto-vincular
        if (usuario.getOrganizacion() == null && usuario.isVerificado() && usuario.getRol() == Rol.FUNDACION_REFUGIO) {
            Fundacion autoFundacion = Fundacion.builder()
                    .nombre(usuario.getNombre())
                    .nit("NIT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                    .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                    .aceptaCustodia(true)
                    .capacidadAlbergue(50)
                    .municipio(usuario.getMunicipioInteres())
                    .fechaVerificacion(Instant.now())
                    .build();
            usuario.setOrganizacion(autoFundacion);
            usuario = usuarioRepository.save(usuario);
        }

        // Validar que es una fundación verificada
        if (usuario.getOrganizacion() == null) {
            throw new AccessDeniedException("Solo las fundaciones pueden gestionar la custodia");
        }

        Object organizacion = Hibernate.unproxy(usuario.getOrganizacion());
        if (!(organizacion instanceof Fundacion fundacion)) {
            throw new AccessDeniedException("Solo las fundaciones pueden gestionar la custodia");
        }

        if (fundacion.getEstadoVerificacion() != EstadoVerificacion.VERIFICADA) {
            throw new AccessDeniedException("La fundación debe estar verificada para gestionar custodia");
        }

        // Obtener el caso
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        String accion = dto.accion() != null ? dto.accion().toUpperCase().trim() : "";

        if ("ASUMIR".equals(accion)) {
            // Validar que la fundación acepta custodia
            if (!fundacion.isAceptaCustodia()) {
                throw new AccessDeniedException("Esta fundación no acepta custodia de animales");
            }

            // Validar que no tiene custodia asignada
            if (caso.getFundacionCustodio() != null) {
                throw new ConflictException("El caso ya tiene un custodio asignado");
            }

            // Validar capacidad de albergue
            long casosEnCustodia = casoRepository.countByFundacionCustodio(fundacion);
            if (casosEnCustodia >= fundacion.getCapacidadAlbergue()) {
                throw new ConflictException("La fundación ha alcanzado su capacidad máxima de albergue");
            }

            // Asumir custodia
            Instant now = Instant.now();
            EstadoCaso estadoAnterior = caso.getEstado();

            caso.setFundacionCustodio(fundacion);
            caso.setFechaCustodia(now);
            caso.setFechaUltimaActividad(now);

            // Solo cambia a EN_ATENCION si está en REGISTRADO
            if (estadoAnterior == EstadoCaso.REGISTRADO) {
                caso.setEstado(EstadoCaso.EN_ATENCION);
            }

            caso = casoRepository.save(caso);

            // Registrar en historial de estado
            HistorialEstado historial = HistorialEstado.builder()
                    .estadoAnterior(estadoAnterior)
                    .estadoNuevo(caso.getEstado())
                    .observacion("Custodia asumida por " + fundacion.getNombre())
                    .fecha(now)
                    .caso(caso)
                    .usuario(usuario)
                    .build();
            historialEstadoRepository.save(historial);

            // Registrar seguimiento
            Seguimiento seguimiento = Seguimiento.builder()
                    .tipo(TipoAporte.ACTUALIZACION)
                    .comentario("Custodia asumida por la fundación: " + fundacion.getNombre())
                    .fecha(now)
                    .caso(caso)
                    .autor(usuario)
                    .visible(true)
                    .build();
            seguimientoRepository.save(seguimiento);

            // Notificar al reportante (vía worker async)
            if (caso.getReportante() != null) {
                Hibernate.initialize(caso.getReportante());
            }
            if (caso.getAnimal() != null) {
                Hibernate.initialize(caso.getAnimal());
            }
            if (caso.getBarrio() != null) {
                Hibernate.initialize(caso.getBarrio());
            }
            notificacionWorker.notificarAsuncionCustodia(caso, fundacion);

        } else if ("LIBERAR".equals(accion)) {
            // Validar que la fundación es el custodio actual
            if (caso.getFundacionCustodio() == null) {
                throw new ConflictException("El caso no tiene custodia asignada");
            }

            if (!caso.getFundacionCustodio().getId().equals(fundacion.getId())) {
                throw new AccessDeniedException("Solo el custodio actual puede liberar la custodia");
            }

            // Validar motivo
            String motivo = dto.motivo();
            if (motivo == null || motivo.trim().isBlank()) {
                throw new IllegalArgumentException("El motivo es obligatorio para liberar la custodia");
            }

            // Liberar custodia
            Instant now = Instant.now();
            EstadoCaso estadoAnterior = caso.getEstado();

            caso.setFundacionCustodio(null);
            caso.setFechaCustodia(null);
            caso.setFechaUltimaActividad(now);

            // Cambiar a REGISTRADO si estaba en EN_ATENCION
            if (estadoAnterior == EstadoCaso.EN_ATENCION) {
                caso.setEstado(EstadoCaso.REGISTRADO);
            }

            caso = casoRepository.save(caso);

            // Registrar en historial de estado
            HistorialEstado historial = HistorialEstado.builder()
                    .estadoAnterior(estadoAnterior)
                    .estadoNuevo(caso.getEstado())
                    .observacion("Custodia liberada. Motivo: " + motivo)
                    .fecha(now)
                    .caso(caso)
                    .usuario(usuario)
                    .build();
            historialEstadoRepository.save(historial);

            // Registrar seguimiento
            Seguimiento seguimiento = Seguimiento.builder()
                    .tipo(TipoAporte.ACTUALIZACION)
                    .comentario("Custodia liberada por la fundación. Motivo: " + motivo)
                    .fecha(now)
                    .caso(caso)
                    .autor(usuario)
                    .visible(true)
                    .build();
            seguimientoRepository.save(seguimiento);

            notificacionWorker.notificarLiberacionCustodia(caso, motivo);

        } else {
            throw new IllegalArgumentException("Acción inválida. Use ASUMIR o LIBERAR");
        }

        return casoMapper.toResponseDTO(caso);
    }

    // 13.4: Listar casos en custodia de la fundación autenticada
    @Override
    @Transactional(readOnly = false)
    public Page<CasoListItemDTO> listarCasosEnCustodia(String correoFundacion, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByCorreo(correoFundacion.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoFundacion));

        if (usuario.getOrganizacion() == null && usuario.isVerificado() && usuario.getRol() == Rol.FUNDACION_REFUGIO) {
            Fundacion autoFundacion = Fundacion.builder()
                    .nombre(usuario.getNombre())
                    .nit("NIT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                    .estadoVerificacion(EstadoVerificacion.VERIFICADA)
                    .aceptaCustodia(true)
                    .capacidadAlbergue(50)
                    .municipio(usuario.getMunicipioInteres())
                    .fechaVerificacion(Instant.now())
                    .build();
            usuario.setOrganizacion(autoFundacion);
            usuario = usuarioRepository.save(usuario);
        }

        if (usuario.getOrganizacion() == null) {
            throw new AccessDeniedException("Solo las fundaciones pueden consultar sus casos en custodia");
        }

        Object organizacion = Hibernate.unproxy(usuario.getOrganizacion());
        if (!(organizacion instanceof Fundacion fundacion)) {
            throw new AccessDeniedException("Solo las fundaciones pueden consultar sus casos en custodia");
        }

        return casoRepository.findByFundacionCustodioAndVisibleTrue(fundacion, pageable)
                .map(casoMapper::toListItemDTO);
    }

    // 13.4: Listar casos sin custodia en un municipio
    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> listarCasosSinCustodia(Long municipioId, Pageable pageable) {
        // Validar que el municipio existe
        if (municipioId == null) {
            throw new IllegalArgumentException("El municipioId es obligatorio");
        }

        return casoRepository.findBySinCustodiaAndMunicipioId(municipioId, pageable)
                .map(casoMapper::toListItemDTO);
    }
}

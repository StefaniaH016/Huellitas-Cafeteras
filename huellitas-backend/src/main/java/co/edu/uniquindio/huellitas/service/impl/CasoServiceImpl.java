package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.AporteMapper;
import co.edu.uniquindio.huellitas.mapper.CasoMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.CasoService;
import co.edu.uniquindio.huellitas.service.CloudinaryService;
import co.edu.uniquindio.huellitas.specification.CasoSpecification;
import co.edu.uniquindio.huellitas.worker.FiltroWorker;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));

        EstadoCaso anterior = caso.getEstado();
        EstadoCaso nuevo = dto.estado();

        Instant now = Instant.now();
        caso.setEstado(nuevo);
        caso.setFechaUltimaActividad(now);

        if (nuevo == EstadoCaso.ARCHIVADO) {
            caso.setFechaArchivado(now);
            if (dto.observacion() != null) {
                caso.setMotivoModeracion(dto.observacion());
            }
        }

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
    public CasoResponseDTO cerrarCaso(Long id, CierreCasoRequestDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));

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

        // W-01: notificar cierre de caso
        notificacionWorker.notificarCierreCaso(caso, dto.motivoCierre().name());

        return casoMapper.toResponseDTO(caso);
    }

    @Override
    public CasoResponseDTO archivarCaso(Long id, ArchivarCasoDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));

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

        return casoMapper.toResponseDTO(caso);
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

        // Validar que es una fundación verificada
        if (usuario.getOrganizacion() == null) {
            throw new AccessDeniedException("Solo las fundaciones pueden gestionar la custodia");
        }

        if (!(usuario.getOrganizacion() instanceof Fundacion fundacion)) {
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

        } else {
            throw new IllegalArgumentException("Acción inválida. Use ASUMIR o LIBERAR");
        }

        return casoMapper.toResponseDTO(caso);
    }

    // 13.4: Listar casos en custodia de la fundación autenticada
    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> listarCasosEnCustodia(String correoFundacion, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByCorreo(correoFundacion.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoFundacion));

        if (usuario.getOrganizacion() == null || !(usuario.getOrganizacion() instanceof Fundacion fundacion)) {
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

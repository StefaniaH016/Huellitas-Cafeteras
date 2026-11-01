package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
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
        return casoRepository.findAll(CasoSpecification.conFiltros(filtros), pageable)
                .map(casoMapper::toListItemDTO);
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
}

package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.CasoService;
import co.edu.uniquindio.huellitas.specification.CasoSpecification;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
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

    @Override
    public CasoResponseDTO crearCaso(CrearReporteDTO dto, String correoUsuario) {
        Usuario reportante = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));

        Barrio barrio = barrioRepository.findById(dto.barrioId())
                .orElseThrow(() -> new ResourceNotFoundException("Barrio", dto.barrioId()));

        AnimalReporteDTO animalDto = dto.animal();
        Animal animal = Animal.builder()
                .nombre(animalDto.nombre())
                .especie(animalDto.especie())
                .raza(animalDto.raza())
                .color(animalDto.color())
                .sexo(animalDto.sexo())
                .edadAproximada(animalDto.edadAproximada())
                .senasParticulares(animalDto.senasParticulares())
                .propietario(reportante)
                .build();
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

        return mapearACasoResponseDTO(caso);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> listarCasos(FiltrosCasoDTO filtros, Pageable pageable) {
        return casoRepository.findAll(CasoSpecification.conFiltros(filtros), pageable)
                .map(this::mapearACasoListItemDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CasoDetalleDTO obtenerDetalle(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", id));

        AnimalDetalleDTO animalDTO = null;
        if (caso.getAnimal() != null) {
            Animal a = caso.getAnimal();
            animalDTO = AnimalDetalleDTO.builder()
                    .id(a.getId())
                    .nombre(a.getNombre())
                    .especie(a.getEspecie())
                    .raza(a.getRaza())
                    .color(a.getColor())
                    .sexo(a.getSexo())
                    .edadAproximada(a.getEdadAproximada())
                    .senasParticulares(a.getSenasParticulares())
                    .build();
        }

        UbicacionDetalleDTO ubicacionDTO = null;
        if (caso.getBarrio() != null) {
            Barrio b = caso.getBarrio();
            Municipio m = b.getMunicipio();
            Departamento d = m != null ? m.getDepartamento() : null;

            ubicacionDTO = UbicacionDetalleDTO.builder()
                    .barrioId(b.getId())
                    .barrio(b.getNombre())
                    .municipioId(m != null ? m.getId() : null)
                    .municipio(m != null ? m.getNombre() : null)
                    .departamentoId(d != null ? d.getId() : null)
                    .departamento(d != null ? d.getNombre() : null)
                    .direccionReferencia(caso.getDireccionReferencia())
                    .build();
        }

        List<FotoDetalleDTO> fotosDTO = (caso.getFotos() != null)
                ? caso.getFotos().stream()
                .map(f -> FotoDetalleDTO.builder()
                        .id(f.getId())
                        .url(f.getUrl())
                        .principal(f.isPrincipal())
                        .build())
                .toList()
                : Collections.emptyList();

        List<HistorialEstadoDTO> historialDTO = historialEstadoRepository.findByCasoIdOrderByFechaAsc(id)
                .stream()
                .map(h -> HistorialEstadoDTO.builder()
                        .id(h.getId())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .observacion(h.getObservacion())
                        .fecha(FechaUtils.formatear(h.getFecha()))
                        .build())
                .toList();

        List<AporteDetalleDTO> aportesDTO = seguimientoRepository.findByCasoIdAndVisibleTrueOrderByFechaAsc(id)
                .stream()
                .map(s -> AporteDetalleDTO.builder()
                        .id(s.getId())
                        .tipo(s.getTipo())
                        .comentario(s.getComentario())
                        .fecha(FechaUtils.formatear(s.getFecha()))
                        .autorNombre(s.getAutor() != null ? s.getAutor().getNombre() : null)
                        .build())
                .toList();

        ResumenMedicoDTO resumenMedicoDTO = null;
        if (caso.getBitacoraMedica() != null) {
            BitacoraMedica bm = caso.getBitacoraMedica();
            resumenMedicoDTO = ResumenMedicoDTO.builder()
                    .id(bm.getId())
                    .fechaApertura(FechaUtils.formatear(bm.getFechaApertura()))
                    .altaMedica(bm.isAltaMedica())
                    .fechaAlta(FechaUtils.formatear(bm.getFechaAlta()))
                    .veterinariaNombre(bm.getVeterinaria() != null ? bm.getVeterinaria().getNombre() : null)
                    .build();
        }

        return CasoDetalleDTO.builder()
                .id(caso.getId())
                .tipoCaso(caso.getTipoCaso())
                .estado(caso.getEstado())
                .urgencia(caso.getUrgencia())
                .descripcion(caso.getDescripcion())
                .animal(animalDTO)
                .ubicacion(ubicacionDTO)
                .fotografias(fotosDTO)
                .historial(historialDTO)
                .aportes(aportesDTO)
                .resumenMedico(resumenMedicoDTO)
                .fechaCreacion(FechaUtils.formatear(caso.getFechaRegistro()))
                .autorId(caso.getReportante() != null ? caso.getReportante().getId() : null)
                .autorNombre(caso.getReportante() != null ? caso.getReportante().getNombre() : null)
                .build();
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

        return mapearACasoResponseDTO(caso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoDTO> obtenerHistorial(Long id) {
        if (!casoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Caso", id);
        }

        return historialEstadoRepository.findByCasoIdOrderByFechaAsc(id)
                .stream()
                .map(h -> HistorialEstadoDTO.builder()
                        .id(h.getId())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .observacion(h.getObservacion())
                        .fecha(FechaUtils.formatear(h.getFecha()))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> misReportes(String correoUsuario, Pageable pageable) {
        return casoRepository.findByReportanteCorreoOrderByFechaRegistroDesc(correoUsuario.trim().toLowerCase(), pageable)
                .map(this::mapearACasoListItemDTO);
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

        return mapearACasoResponseDTO(caso);
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

        return mapearACasoResponseDTO(caso);
    }

    private CasoResponseDTO mapearACasoResponseDTO(Caso caso) {
        return CasoResponseDTO.builder()
                .id(caso.getId())
                .tipoCaso(caso.getTipoCaso())
                .urgencia(caso.getUrgencia())
                .estado(caso.getEstado())
                .descripcion(caso.getDescripcion())
                .direccionReferencia(caso.getDireccionReferencia())
                .autorId(caso.getReportante() != null ? caso.getReportante().getId() : null)
                .barrioId(caso.getBarrio() != null ? caso.getBarrio().getId() : null)
                .fechaCreacion(FechaUtils.formatear(caso.getFechaRegistro()))
                .build();
    }

    private CasoListItemDTO mapearACasoListItemDTO(Caso caso) {
        String departamento = null;
        String municipio = null;
        String barrio = null;

        if (caso.getBarrio() != null) {
            barrio = caso.getBarrio().getNombre();
            if (caso.getBarrio().getMunicipio() != null) {
                municipio = caso.getBarrio().getMunicipio().getNombre();
                if (caso.getBarrio().getMunicipio().getDepartamento() != null) {
                    departamento = caso.getBarrio().getMunicipio().getDepartamento().getNombre();
                }
            }
        }

        return CasoListItemDTO.builder()
                .id(caso.getId())
                .tipoCaso(caso.getTipoCaso())
                .estado(caso.getEstado())
                .urgencia(caso.getUrgencia())
                .descripcion(caso.getDescripcion())
                .especie(caso.getAnimal() != null ? caso.getAnimal().getEspecie() : null)
                .fotoPrincipal(extraerFotoPrincipal(caso))
                .departamento(departamento)
                .municipio(municipio)
                .barrio(barrio)
                .fechaCreacion(FechaUtils.formatear(caso.getFechaRegistro()))
                .build();
    }

    private String extraerFotoPrincipal(Caso caso) {
        if (caso.getFotos() == null || caso.getFotos().isEmpty()) {
            return null;
        }
        return caso.getFotos().stream()
                .filter(Foto::isPrincipal)
                .map(Foto::getUrl)
                .findFirst()
                .orElseGet(() -> caso.getFotos().get(0).getUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CasoListItemDTO> getFeed(Pageable pageable) {
        List<Urgencia> urgentes = List.of(Urgencia.ALTA, Urgencia.CRITICA);
        return casoRepository.findByUrgenciaInAndVisibleTrueOrderByFechaRegistroDesc(urgentes, pageable)
                .map(this::mapearACasoListItemDTO);
    }
}

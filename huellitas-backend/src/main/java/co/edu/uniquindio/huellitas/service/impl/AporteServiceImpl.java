package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.aporte.AporteResponseDTO;
import co.edu.uniquindio.huellitas.dto.aporte.CrearAporteDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.AporteMapper;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.FotoRepository;
import co.edu.uniquindio.huellitas.repository.SeguimientoRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.AporteService;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AporteServiceImpl implements AporteService {

    private static final int MIN_DESCRIPCION_LENGTH = 10;
    private static final int MAX_DESCRIPCION_LENGTH = 1000;

    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final FotoRepository fotoRepository;
    private final BarrioRepository barrioRepository;
    private final AporteMapper aporteMapper;
    private final NotificacionWorker notificacionWorker;

    @Override
    @Transactional(readOnly = true)
    public List<AporteResponseDTO> listarAportes(Long casoId) {
        if (!casoRepository.existsById(casoId)) {
            throw new ResourceNotFoundException("Caso", casoId);
        }

        List<Seguimiento> seguimientos = seguimientoRepository.findByCasoIdAndVisibleTrueOrderByFechaAsc(casoId);
        return seguimientos.stream()
                .map(s -> aporteMapper.toResponseDTO(s, casoId))
                .toList();
    }

    @Override
    public AporteResponseDTO crearAporte(Long casoId, CrearAporteDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        if (caso.getEstado() != EstadoCaso.REGISTRADO && caso.getEstado() != EstadoCaso.EN_ATENCION) {
            throw new ConflictException("No se pueden agregar aportes a un caso " + caso.getEstado().name().toLowerCase() 
                    + ". Los casos archivados o resueltos son de solo lectura.");
        }

        Usuario autor = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));
        validarPermisoAporte(caso, autor);

        // 13.1: Validar longitud de la descripción
        String descripcion = dto.descripcion();
        if (descripcion == null || descripcion.trim().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        if (descripcion.trim().length() < MIN_DESCRIPCION_LENGTH) {
            throw new IllegalArgumentException("La descripción debe tener al menos " + MIN_DESCRIPCION_LENGTH + " caracteres");
        }
        if (descripcion.trim().length() > MAX_DESCRIPCION_LENGTH) {
            throw new IllegalArgumentException("La descripción no puede exceder " + MAX_DESCRIPCION_LENGTH + " caracteres");
        }

        // 13.1: Validar barrioId opcional
        Barrio barrio = null;
        if (dto.barrioId() != null) {
            barrio = barrioRepository.findById(dto.barrioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barrio", dto.barrioId()));
        }

        TipoAporte tipo = (dto.ubicacionTexto() != null && !dto.ubicacionTexto().isBlank())
                ? TipoAporte.AVISTAMIENTO
                : TipoAporte.COMENTARIO;

        Seguimiento seguimiento = Seguimiento.builder()
                .caso(caso)
                .autor(autor)
                .tipo(tipo)
                .comentario(descripcion.trim())
                .ubicacionTexto(tipo == TipoAporte.AVISTAMIENTO ? dto.ubicacionTexto().trim() : null)
                .fecha(Instant.now())
                .visible(true)
                .barrio(barrio)
                .build();

        Seguimiento guardado = seguimientoRepository.save(seguimiento);

        List<String> fotosGuardadas = new ArrayList<>();
        if (dto.fotografias() != null && !dto.fotografias().isEmpty()) {
            for (String url : dto.fotografias()) {
                if (url != null && !url.isBlank()) {
                    Foto foto = fotoRepository.findFirstByCasoIdAndUrlAndSeguimientoIsNull(casoId, url)
                            .orElseGet(() -> Foto.builder()
                                    .caso(caso)
                                    .url(url)
                                    .principal(false)
                                    .build());
                    foto.setSeguimiento(guardado);
                    foto.setPrincipal(false);
                    fotoRepository.save(foto);
                    guardado.getFotografias().add(foto);
                    fotosGuardadas.add(url);
                }
            }
        }

        caso.setFechaUltimaActividad(Instant.now());
        casoRepository.save(caso);

        // 13.5: Notificar a los involucrados (excepto el autor)
        try {
            notificacionWorker.notificarAporte(guardado);
        } catch (Exception e) {
            log.warn("Error al procesar notificaciones del aporte: {}", e.getMessage());
        }

        return aporteMapper.toResponseDTO(guardado, caso.getId(), autor.getId(), dto.ubicacionTexto(), fotosGuardadas);
    }

    private void validarPermisoAporte(Caso caso, Usuario autor) {
        if (autor.getRol() == Rol.CIUDADANO_VOLUNTARIO) {
            boolean esReportante = caso.getReportante() != null
                    && Objects.equals(caso.getReportante().getId(), autor.getId());
            if (!esReportante || caso.getFundacionCustodio() == null) {
                return;
            }
        } else if (autor.getRol() == Rol.FUNDACION_REFUGIO
                && autor.getOrganizacion() instanceof Fundacion fundacion
                && fundacion.getEstadoVerificacion() == EstadoVerificacion.VERIFICADA
                && caso.getFundacionCustodio() != null
                && Objects.equals(caso.getFundacionCustodio().getId(), fundacion.getId())) {
            return;
        }

        throw new AccessDeniedException("El usuario no tiene permiso para agregar aportes a este caso.");
    }
}

package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ReportarCasoDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.ModeracionMapper;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.SeguimientoRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.ModeracionService;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ModeracionServiceImpl implements ModeracionService {

    private final CasoRepository casoRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModeracionMapper moderacionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CasoModeracionDTO> listarCasosReportados() {
        List<Caso> casos = casoRepository.findByModeracionPendienteTrueOrVisibleFalse();
        return casos.stream()
                .map(moderacionMapper::toCasoModeracionDTO)
                .toList();
    }

    @Override
    public ModeracionResponseDTO moderarCaso(Long casoId, DecisionModeracionDTO dto, String correoAdmin) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        boolean mantener = "MANTENER".equalsIgnoreCase(dto.decision());
        caso.setVisible(mantener);
        caso.setModeracionPendiente(false);
        caso.setMotivoModeracion(dto.motivo());
        caso.setFechaUltimaActividad(Instant.now());

        casoRepository.save(caso);

        return moderacionMapper.toModeracionResponseDTO(caso.getId(), mantener);
    }

    @Override
    public ModeracionResponseDTO reportarCaso(Long casoId, ReportarCasoDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));
        Usuario reportante = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (reportante.getRol() != Rol.CIUDADANO_VOLUNTARIO
                || caso.getReportante() == null
                || Objects.equals(caso.getReportante().getId(), reportante.getId())) {
            throw new AccessDeniedException("No tienes permiso para reportar este caso.");
        }
        if (!caso.isVisible()) {
            throw new ConflictException("Este caso ya no está disponible para reportes.");
        }
        if (caso.isModeracionPendiente()) {
            return new ModeracionResponseDTO(
                    casoId, "YA_REPORTADO", FechaUtils.formatear(Instant.now()));
        }

        caso.setMotivoModeracion(dto.motivo().trim());
        caso.setModeracionPendiente(true);
        casoRepository.save(caso);

        return new ModeracionResponseDTO(
                casoId, "REPORTADO", FechaUtils.formatear(Instant.now()));
    }

    @Override
    public ModeracionResponseDTO ocultarAporte(Long aporteId, String motivo, String correoAdmin) {
        Seguimiento aporte = seguimientoRepository.findById(aporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Aporte", aporteId));

        if (!aporte.isVisible()) {
            return moderacionMapper.toModeracionResponseDTO(aporteId, false);
        }

        aporte.setVisible(false);
        aporte.setMotivoOcultacion(motivo);
        seguimientoRepository.save(aporte);

        return moderacionMapper.toModeracionResponseDTO(aporteId, false);
    }
}

package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.service.ModeracionService;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ModeracionServiceImpl implements ModeracionService {

    private final CasoRepository casoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CasoModeracionDTO> listarCasosReportados() {
        List<Caso> casos = casoRepository.findByMotivoModeracionNotNullOrVisibleFalse();
        return casos.stream().map(c -> new CasoModeracionDTO(
                c.getId(),
                c.getMotivoModeracion() != null ? c.getMotivoModeracion() : "Reportado para revisión",
                c.isVisible() ? "PENDIENTE" : "OCULTADO",
                FechaUtils.formatear(c.getFechaRegistro())
        )).toList();
    }

    @Override
    public ModeracionResponseDTO moderarCaso(Long casoId, DecisionModeracionDTO dto, String correoAdmin) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        boolean mantener = "MANTENER".equalsIgnoreCase(dto.decision());
        caso.setVisible(mantener);
        caso.setMotivoModeracion(dto.motivo());
        caso.setFechaUltimaActividad(Instant.now());

        casoRepository.save(caso);

        return new ModeracionResponseDTO(
                caso.getId(),
                mantener ? "MANTENIDO" : "OCULTADO",
                FechaUtils.formatear(Instant.now())
        );
    }
}

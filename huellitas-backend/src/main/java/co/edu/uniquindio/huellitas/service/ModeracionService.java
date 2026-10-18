package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;

import java.util.List;

public interface ModeracionService {
    List<CasoModeracionDTO> listarCasosReportados();
    ModeracionResponseDTO moderarCaso(Long casoId, DecisionModeracionDTO dto, String correoAdmin);
}

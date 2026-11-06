package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.moderacion.CasoModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.DecisionModeracionDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ModeracionResponseDTO;
import co.edu.uniquindio.huellitas.dto.moderacion.ReportarCasoDTO;

import java.util.List;

public interface ModeracionService {
    List<CasoModeracionDTO> listarCasosReportados();
    ModeracionResponseDTO moderarCaso(Long casoId, DecisionModeracionDTO dto, String correoAdmin);
    ModeracionResponseDTO reportarCaso(Long casoId, ReportarCasoDTO dto, String correoUsuario);

    /**
     * 13.2: Oculta un aporte (Seguimiento) registrando el motivo.
     * Solo administrators pueden ocultar aportes.
     */
    ModeracionResponseDTO ocultarAporte(Long aporteId, String motivo, String correoAdmin);
}

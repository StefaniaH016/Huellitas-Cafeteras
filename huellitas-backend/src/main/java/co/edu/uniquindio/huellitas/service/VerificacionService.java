package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;

import java.util.List;

public interface VerificacionService {
    List<VerificacionPendienteDTO> listarPendientes();
    VerificacionResponseDTO resolverVerificacion(Long usuarioId, DecisionVerificacionDTO dto, String correoAdmin);
}

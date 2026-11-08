package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.DocumentoSoporteDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VerificacionService {
    List<VerificacionPendienteDTO> listarPendientes();
    VerificacionResponseDTO resolverVerificacion(Long usuarioId, DecisionVerificacionDTO dto, String correoAdmin);
    VerificacionResponseDTO solicitarRevision(
            String correo,
            MultipartFile documentoSoporte,
            String nombreEntidad,
            String nit,
            String municipioOperacion);
    DocumentoSoporteDTO obtenerDocumentoSoporte(Long usuarioId);
}

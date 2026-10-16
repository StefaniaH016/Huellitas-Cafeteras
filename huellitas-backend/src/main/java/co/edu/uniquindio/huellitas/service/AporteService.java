package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.aporte.AporteResponseDTO;
import co.edu.uniquindio.huellitas.dto.aporte.CrearAporteDTO;

import java.util.List;

public interface AporteService {
    List<AporteResponseDTO> listarAportes(Long casoId);
    AporteResponseDTO crearAporte(Long casoId, CrearAporteDTO dto, String correoUsuario);
}

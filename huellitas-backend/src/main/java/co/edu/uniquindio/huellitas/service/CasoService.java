package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CasoService {

    CasoResponseDTO crearCaso(CrearReporteDTO dto, String correoUsuario);

    Page<CasoListItemDTO> listarCasos(FiltrosCasoDTO filtros, Pageable pageable);

    CasoDetalleDTO obtenerDetalle(Long id);

    CasoResponseDTO actualizarEstado(Long id, ActualizarEstadoDTO dto, String correoUsuario);

    List<HistorialEstadoDTO> obtenerHistorial(Long id);

    Page<CasoListItemDTO> misReportes(String correoUsuario, Pageable pageable);

    CasoResponseDTO cerrarCaso(Long id, CierreCasoRequestDTO dto, String correoUsuario);

    CasoResponseDTO archivarCaso(Long id, ArchivarCasoDTO dto, String correoUsuario);

    Page<CasoListItemDTO> getFeed(Pageable pageable);
}

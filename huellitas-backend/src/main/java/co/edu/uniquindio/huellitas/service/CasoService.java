package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CasoService {

    CasoResponseDTO crearCaso(CrearReporteDTO dto, String correoUsuario);

    CasoResponseDTO actualizarReporte(Long id, CrearReporteDTO dto, String correoUsuario);

    void eliminarFoto(Long casoId, Long fotoId, String correoUsuario);

    void eliminarCaso(Long id, String correoUsuario);

    Page<CasoListItemDTO> listarCasos(FiltrosCasoDTO filtros, Pageable pageable);

    CasoDetalleDTO obtenerDetalle(Long id);

    CasoResponseDTO actualizarEstado(Long id, ActualizarEstadoDTO dto, String correoUsuario);

    List<HistorialEstadoDTO> obtenerHistorial(Long id);

    Page<CasoListItemDTO> misReportes(String correoUsuario, Pageable pageable);

    CasoResponseDTO cerrarCaso(Long id, CierreCasoRequestDTO dto, String correoUsuario);

    CasoResponseDTO archivarCaso(Long id, ArchivarCasoDTO dto, String correoUsuario);

    Page<CasoListItemDTO> getFeed(Pageable pageable);

    // 13.3: Gestión de custodia
    CasoResponseDTO gestionarCustodia(Long casoId, CustodiaDTO dto, String correoUsuario);

    // 13.4: Casos en custodia de la fundación autenticada
    Page<CasoListItemDTO> listarCasosEnCustodia(String correoFundacion, Pageable pageable);

    // 13.4: Casos sin custodia en un municipio
    Page<CasoListItemDTO> listarCasosSinCustodia(Long municipioId, Pageable pageable);
}

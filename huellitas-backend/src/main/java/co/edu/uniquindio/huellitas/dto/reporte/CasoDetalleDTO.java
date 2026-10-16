package co.edu.uniquindio.huellitas.dto.reporte;

import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import lombok.Builder;

import java.util.List;

@Builder
public record CasoDetalleDTO(
        Long id,
        TipoCaso tipoCaso,
        EstadoCaso estado,
        Urgencia urgencia,
        String descripcion,
        AnimalDetalleDTO animal,
        UbicacionDetalleDTO ubicacion,
        List<FotoDetalleDTO> fotografias,
        List<HistorialEstadoDTO> historial,
        List<AporteDetalleDTO> aportes,
        ResumenMedicoDTO resumenMedico,
        String fechaCreacion,
        Long autorId,
        String autorNombre
) {}

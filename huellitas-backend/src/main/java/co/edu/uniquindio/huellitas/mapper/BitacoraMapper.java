package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import co.edu.uniquindio.huellitas.entity.RegistroMedico;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BitacoraMapper {

    public BitacoraResponseDTO toResponseDTO(BitacoraMedica guardada, Long casoId, Long veterinariaId, String estado) {
        if (guardada == null) {
            return null;
        }
        return new BitacoraResponseDTO(
                guardada.getId(),
                casoId,
                veterinariaId,
                FechaUtils.formatear(guardada.getFechaApertura()),
                estado
        );
    }

    public EvolucionDetalleDTO toEvolucionDetalleDTO(RegistroMedico r) {
        if (r == null) {
            return null;
        }
        return new EvolucionDetalleDTO(
                r.getId(),
                FechaUtils.formatear(r.getFecha()),
                r.getDiagnostico(),
                r.getTratamiento(),
                r.getEvolucion()
        );
    }

    public List<EvolucionDetalleDTO> toEvolucionDetalleDTOList(List<RegistroMedico> registros) {
        if (registros == null) {
            return List.of();
        }
        return registros.stream().map(this::toEvolucionDetalleDTO).toList();
    }

    public BitacoraDetalleDTO toDetalleDTO(BitacoraMedica bitacora, Long casoId, List<EvolucionDetalleDTO> evoluciones) {
        if (bitacora == null) {
            return null;
        }
        Map<String, Object> altaInfo = null;
        if (bitacora.isAltaMedica()) {
            altaInfo = new LinkedHashMap<>();
            altaInfo.put("fechaAlta", FechaUtils.formatear(bitacora.getFechaAlta()));
            altaInfo.put("estado", "ALTA_REGISTRADA");
        }
        String estado = bitacora.isAltaMedica() ? "ALTA_REGISTRADA" : "ABIERTA";
        return new BitacoraDetalleDTO(bitacora.getId(), casoId, estado, evoluciones, altaInfo);
    }

    public RegistroMedicoResponseDTO toRegistroMedicoResponseDTO(RegistroMedico guardado, Long bitacoraId) {
        if (guardado == null) {
            return null;
        }
        return new RegistroMedicoResponseDTO(
                guardado.getId(),
                bitacoraId,
                FechaUtils.formatear(guardado.getFecha())
        );
    }

    public AltaMedicaResponseDTO toAltaMedicaResponseDTO(BitacoraMedica bitacora, Long casoId) {
        if (bitacora == null) {
            return null;
        }
        return new AltaMedicaResponseDTO(
                casoId,
                FechaUtils.formatear(bitacora.getFechaAlta()),
                "ALTA_REGISTRADA"
        );
    }
}


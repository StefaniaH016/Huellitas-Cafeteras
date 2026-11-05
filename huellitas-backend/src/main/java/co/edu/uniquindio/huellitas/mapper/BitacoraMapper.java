package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.entity.Animal;
import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
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
        String firmanteNombre = r.getFirmante() != null ? r.getFirmante().getNombre() : null;
        String fechaFirma = r.getFechaFirma() != null ? FechaUtils.formatear(r.getFechaFirma()) : null;
        return new EvolucionDetalleDTO(
                r.getId(),
                FechaUtils.formatear(r.getFecha()),
                r.getDiagnostico(),
                r.getProcedimiento(),
                r.getTratamiento(),
                r.getMedicamentos(),
                r.getEvolucion(),
                r.getEvolucion(),
                firmanteNombre,
                fechaFirma
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

    public PacienteResponseDTO toPacienteResponseDTO(BitacoraMedica bm) {
        if (bm == null) {
            return null;
        }
        Caso caso = bm.getCaso();
        String nombreAnimal = null;
        String especie = null;
        String raza = null;
        String sexo = null;
        if (caso != null && caso.getAnimal() != null) {
            Animal a = caso.getAnimal();
            nombreAnimal = a.getNombre();
            especie = a.getEspecie() != null ? a.getEspecie().name() : null;
            raza = a.getRaza();
            sexo = a.getSexo() != null ? a.getSexo().name() : null;
        }

        String municipio = null;
        String barrio = null;
        if (caso != null && caso.getBarrio() != null) {
            barrio = caso.getBarrio().getNombre();
            if (caso.getBarrio().getMunicipio() != null) {
                municipio = caso.getBarrio().getMunicipio().getNombre();
            }
        }

        String foto = null;
        if (caso != null && caso.getFotos() != null && !caso.getFotos().isEmpty()) {
            foto = caso.getFotos().stream()
                    .filter(Foto::isPrincipal)
                    .map(Foto::getUrl)
                    .findFirst()
                    .orElseGet(() -> caso.getFotos().get(0).getUrl());
        }

        String estado = bm.isAltaMedica() ? "ALTA_REGISTRADA" : (caso != null && caso.getEstado() != null ? caso.getEstado().name() : "ABIERTA");

        return new PacienteResponseDTO(
                bm.getId(),
                caso != null ? caso.getId() : null,
                nombreAnimal,
                especie,
                raza,
                sexo,
                caso != null ? caso.getDescripcion() : null,
                caso != null && caso.getUrgencia() != null ? caso.getUrgencia().name() : null,
                estado,
                municipio,
                barrio,
                foto,
                FechaUtils.formatear(bm.getFechaApertura()),
                bm.isAltaMedica(),
                FechaUtils.formatear(bm.getFechaAlta()),
                null
        );
    }

    public List<PacienteResponseDTO> toPacienteResponseDTOList(List<BitacoraMedica> lista) {
        if (lista == null) {
            return List.of();
        }
        return lista.stream().map(this::toPacienteResponseDTO).toList();
    }
}


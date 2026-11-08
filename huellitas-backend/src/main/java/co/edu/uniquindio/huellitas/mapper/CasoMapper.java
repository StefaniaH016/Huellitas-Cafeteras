package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.reporte.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CasoMapper {

    public CasoResponseDTO toResponseDTO(Caso caso) {
        if (caso == null) {
            return null;
        }
        return CasoResponseDTO.builder()
                .id(caso.getId())
                .tipoCaso(caso.getTipoCaso())
                .urgencia(caso.getUrgencia())
                .estado(caso.getEstado())
                .descripcion(caso.getDescripcion())
                .direccionReferencia(caso.getDireccionReferencia())
                .autorId(caso.getReportante() != null ? caso.getReportante().getId() : null)
                .barrioId(caso.getBarrio() != null ? caso.getBarrio().getId() : null)
                .fechaCreacion(FechaUtils.formatear(caso.getFechaRegistro()))
                .build();
    }

    public CasoListItemDTO toListItemDTO(Caso caso) {
        if (caso == null) {
            return null;
        }
        String departamento = null;
        String municipio = null;
        String barrio = null;

        if (caso.getBarrio() != null) {
            barrio = caso.getBarrio().getNombre();
            if (caso.getBarrio().getMunicipio() != null) {
                municipio = caso.getBarrio().getMunicipio().getNombre();
                if (caso.getBarrio().getMunicipio().getDepartamento() != null) {
                    departamento = caso.getBarrio().getMunicipio().getDepartamento().getNombre();
                }
            }
        }

        return CasoListItemDTO.builder()
                .id(caso.getId())
                .tipoCaso(caso.getTipoCaso())
                .estado(caso.getEstado())
                .urgencia(caso.getUrgencia())
                .descripcion(caso.getDescripcion())
                .especie(caso.getAnimal() != null ? caso.getAnimal().getEspecie() : null)
                .fotoPrincipal(extraerFotoPrincipal(caso))
                .departamento(departamento)
                .municipio(municipio)
                .barrio(barrio)
                .fechaCreacion(FechaUtils.formatear(caso.getFechaRegistro()))
                .build();
    }

    public String extraerFotoPrincipal(Caso caso) {
        if (caso == null || caso.getFotos() == null || caso.getFotos().isEmpty()) {
            return null;
        }
        return caso.getFotos().stream()
                .filter(foto -> foto.getSeguimiento() == null)
                .filter(Foto::isPrincipal)
                .map(Foto::getUrl)
                .findFirst()
                .orElseGet(() -> caso.getFotos().stream()
                        .filter(foto -> foto.getSeguimiento() == null)
                        .map(Foto::getUrl)
                        .findFirst()
                        .orElse(null));
    }

    public Animal toAnimalEntity(AnimalReporteDTO dto, Usuario propietario) {
        if (dto == null) {
            return null;
        }
        return Animal.builder()
                .nombre(dto.nombre())
                .especie(dto.especie())
                .raza(dto.raza())
                .color(dto.color())
                .sexo(dto.sexo())
                .edadAproximada(dto.edadAproximada())
                .senasParticulares(dto.senasParticulares())
                .propietario(propietario)
                .build();
    }

    public AnimalDetalleDTO toAnimalDetalleDTO(Animal a) {
        if (a == null) {
            return null;
        }
        return AnimalDetalleDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .especie(a.getEspecie())
                .raza(a.getRaza())
                .color(a.getColor())
                .sexo(a.getSexo())
                .edadAproximada(a.getEdadAproximada())
                .senasParticulares(a.getSenasParticulares())
                .build();
    }

    public UbicacionDetalleDTO toUbicacionDetalleDTO(Caso caso) {
        if (caso == null || caso.getBarrio() == null) {
            return null;
        }
        Barrio b = caso.getBarrio();
        Municipio m = b.getMunicipio();
        Departamento d = m != null ? m.getDepartamento() : null;

        return UbicacionDetalleDTO.builder()
                .barrioId(b.getId())
                .barrio(b.getNombre())
                .municipioId(m != null ? m.getId() : null)
                .municipio(m != null ? m.getNombre() : null)
                .departamentoId(d != null ? d.getId() : null)
                .departamento(d != null ? d.getNombre() : null)
                .direccionReferencia(caso.getDireccionReferencia())
                .build();
    }

    public FotoDetalleDTO toFotoDetalleDTO(Foto f) {
        if (f == null) {
            return null;
        }
        return FotoDetalleDTO.builder()
                .id(f.getId())
                .url(f.getUrl())
                .principal(f.isPrincipal())
                .build();
    }

    public List<FotoDetalleDTO> toFotoDetalleDTOList(List<Foto> fotos) {
        if (fotos == null) {
            return Collections.emptyList();
        }
        return fotos.stream()
                .filter(foto -> foto.getSeguimiento() == null)
                .map(this::toFotoDetalleDTO)
                .toList();
    }

    public HistorialEstadoDTO toHistorialEstadoDTO(HistorialEstado h) {
        if (h == null) {
            return null;
        }
        return HistorialEstadoDTO.builder()
                .id(h.getId())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoNuevo(h.getEstadoNuevo())
                .observacion(h.getObservacion())
                .fecha(FechaUtils.formatear(h.getFecha()))
                .build();
    }

    public List<HistorialEstadoDTO> toHistorialEstadoDTOList(List<HistorialEstado> lista) {
        if (lista == null) {
            return Collections.emptyList();
        }
        return lista.stream().map(this::toHistorialEstadoDTO).toList();
    }

    public ResumenMedicoDTO toResumenMedicoDTO(BitacoraMedica bm) {
        if (bm == null) {
            return null;
        }
        return ResumenMedicoDTO.builder()
                .id(bm.getId())
                .fechaApertura(FechaUtils.formatear(bm.getFechaApertura()))
                .altaMedica(bm.isAltaMedica())
                .fechaAlta(FechaUtils.formatear(bm.getFechaAlta()))
                .veterinariaNombre(nombreVeterinariaResponsable(bm))
                .responsableId(bm.getVeterinario() != null ? bm.getVeterinario().getId() : null)
                .build();
    }

    private String nombreVeterinariaResponsable(BitacoraMedica bitacora) {
        if (bitacora.getVeterinaria() != null) {
            return bitacora.getVeterinaria().getNombre();
        }
        if (bitacora.getVeterinario() == null) {
            return null;
        }
        return bitacora.getVeterinario().getOrganizacion() instanceof Veterinaria veterinaria
                ? veterinaria.getNombre()
                : bitacora.getVeterinario().getNombre();
    }

    public CasoDetalleDTO toCasoDetalleDTO(
            Caso caso,
            AnimalDetalleDTO animalDTO,
            UbicacionDetalleDTO ubicacionDTO,
            List<FotoDetalleDTO> fotosDTO,
            List<HistorialEstadoDTO> historialDTO,
            List<AporteDetalleDTO> aportesDTO,
            ResumenMedicoDTO resumenMedicoDTO) {
        if (caso == null) {
            return null;
        }
        return CasoDetalleDTO.builder()
                .id(caso.getId())
                .tipoCaso(caso.getTipoCaso())
                .estado(caso.getEstado())
                .urgencia(caso.getUrgencia())
                .descripcion(caso.getDescripcion())
                .animal(animalDTO)
                .ubicacion(ubicacionDTO)
                .fotografias(fotosDTO)
                .historial(historialDTO)
                .aportes(aportesDTO)
                .resumenMedico(resumenMedicoDTO)
                .fechaCreacion(FechaUtils.formatear(caso.getFechaRegistro()))
                .autorId(caso.getReportante() != null ? caso.getReportante().getId() : null)
                .autorNombre(caso.getReportante() != null ? caso.getReportante().getNombre() : null)
                .fundacionCustodioId(caso.getFundacionCustodio() != null ? caso.getFundacionCustodio().getId() : null)
                .fundacionCustodioNombre(caso.getFundacionCustodio() != null ? caso.getFundacionCustodio().getNombre() : null)
                .build();
    }
}

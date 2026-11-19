package co.edu.uniquindio.huellitas.mapper;

import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Municipio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacionMapperTest {

    private final VerificacionMapper mapper = new VerificacionMapper();

    @Test
    void toPendienteDTOCubreOrganizacionCompletaYSinOrganizacion() {
        assertThat(mapper.toPendienteDTO(null)).isNull();

        Fundacion fundacion = Fundacion.builder()
                .nombre("Refugio Esperanza")
                .nit("900111222")
                .estadoVerificacion(EstadoVerificacion.PENDIENTE)
                .municipio(Municipio.builder().nombre("Pereira").build())
                .documentoSoporteNombre("rut.pdf")
                .build();

        Usuario institucional = Usuario.builder()
                .id(10L)
                .nombre("Contacto Refugio")
                .rol(Rol.FUNDACION_REFUGIO)
                .organizacion(fundacion)
                .build();

        VerificacionPendienteDTO dto = mapper.toPendienteDTO(institucional);
        assertThat(dto.usuarioId()).isEqualTo(10L);
        assertThat(dto.nombreEntidad()).isEqualTo("Refugio Esperanza");
        assertThat(dto.rolSolicitado()).isEqualTo("FUNDACION_REFUGIO");
        assertThat(dto.estado()).isEqualTo("PENDIENTE");
        assertThat(dto.nit()).isEqualTo("900111222");
        assertThat(dto.municipio()).isEqualTo("Pereira");
        assertThat(dto.documentoSoporteNombre()).isEqualTo("rut.pdf");
        assertThat(dto.documentoSoporteDisponible()).isTrue();

        Usuario sinOrg = Usuario.builder()
                .id(11L)
                .nombre("Clínica Sin Org")
                .rol(Rol.VETERINARIA)
                .build();
        VerificacionPendienteDTO dtoSinOrg = mapper.toPendienteDTO(sinOrg);
        assertThat(dtoSinOrg.nombreEntidad()).isEqualTo("Clínica Sin Org");
        assertThat(dtoSinOrg.estado()).isEqualTo("PENDIENTE");
        assertThat(dtoSinOrg.nit()).isNull();
        assertThat(dtoSinOrg.municipio()).isNull();
        assertThat(dtoSinOrg.documentoSoporteDisponible()).isFalse();
    }

    @Test
    void toResponseDTOCubreConYSinOrganizacionYNull() {
        assertThat(mapper.toResponseDTO(null, true)).isNull();

        Usuario sinOrgAprobado = Usuario.builder().id(1L).verificado(true).build();
        VerificacionResponseDTO r1 = mapper.toResponseDTO(sinOrgAprobado, true);
        assertThat(r1.estado()).isEqualTo("VERIFICADA");
        assertThat(r1.verificado()).isTrue();

        Usuario sinOrgRechazado = Usuario.builder().id(2L).verificado(false).build();
        VerificacionResponseDTO r2 = mapper.toResponseDTO(sinOrgRechazado, false);
        assertThat(r2.estado()).isEqualTo("RECHAZADA");
        assertThat(r2.verificado()).isFalse();

        Usuario conOrg = Usuario.builder()
                .id(3L)
                .verificado(true)
                .organizacion(Fundacion.builder().estadoVerificacion(EstadoVerificacion.VERIFICADA).build())
                .build();
        VerificacionResponseDTO r3 = mapper.toResponseDTO(conOrg, true);
        assertThat(r3.estado()).isEqualTo("VERIFICADA");
    }
}

package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.reporte.CasoResponseDTO;
import co.edu.uniquindio.huellitas.dto.reporte.CrearReporteDTO;
import co.edu.uniquindio.huellitas.service.CasoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.mock.web.MockMultipartFile;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock
    private CasoService casoService;

    @InjectMocks
    private ReporteController reporteController;

    private final UserDetails usuario = User.withUsername("usuario@test.com")
            .password("ignored")
            .authorities("ROLE_CIUDADANO")
            .build();

    @Test
    void actualizarReporteRetornaCasoCuandoUsuarioEstaAutenticado() {
        CrearReporteDTO dto = null;
        CasoResponseDTO caso = CasoResponseDTO.builder().id(12L).build();
        when(casoService.actualizarReporte(12L, dto, usuario.getUsername())).thenReturn(caso);

        var response = reporteController.actualizarReporte(12L, dto, usuario);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caso, response.getBody());
        verify(casoService).actualizarReporte(12L, dto, usuario.getUsername());
    }

    @Test
    void actualizarReporteRechazaUsuarioNoAutenticado() {
        var response = reporteController.actualizarReporte(12L, null, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(casoService);
    }

    @Test
    void crearReporteConImagenRetornaCreadoCuandoUsuarioEstaAutenticado() {
        CrearReporteDTO dto = null;
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "animal.jpg", "image/jpeg", new byte[] {1});
        CasoResponseDTO caso = CasoResponseDTO.builder().id(12L).build();
        when(casoService.crearCasoConImagen(dto, List.of(archivo), usuario.getUsername())).thenReturn(caso);

        var response = reporteController.crearReporteConImagen(dto, List.of(archivo), usuario);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(caso, response.getBody());
        verify(casoService).crearCasoConImagen(dto, List.of(archivo), usuario.getUsername());
    }

    @Test
    void crearReporteConImagenRechazaUsuarioNoAutenticado() {
        var response = reporteController.crearReporteConImagen(
                null,
                List.of(new MockMultipartFile("archivos", "animal.jpg", "image/jpeg", new byte[] {1})),
                null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(casoService);
    }

    @Test
    void eliminarCasoRetornaNoContentCuandoUsuarioEstaAutenticado() {
        var response = reporteController.eliminarCaso(12L, usuario);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(casoService).eliminarCaso(12L, usuario.getUsername());
    }

    @Test
    void eliminarCasoRechazaUsuarioNoAutenticado() {
        var response = reporteController.eliminarCaso(12L, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(casoService);
    }

    @Test
    void eliminarFotoRetornaNoContentCuandoUsuarioEstaAutenticado() {
        var response = reporteController.eliminarFoto(12L, 5L, usuario);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(casoService).eliminarFoto(12L, 5L, usuario.getUsername());
    }

    @Test
    void eliminarFotoRechazaUsuarioNoAutenticado() {
        var response = reporteController.eliminarFoto(12L, 5L, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(casoService);
    }
}

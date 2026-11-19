package co.edu.uniquindio.huellitas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private WebRequest crearRequest(String uri) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI(uri);
        return new ServletWebRequest(servletRequest);
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleCloudinaryIntegrationRetorna502BadGatewayConEstructuraEstandar() {
        CloudinaryIntegrationException ex = new CloudinaryIntegrationException(
                "No fue posible procesar y optimizar la imagen con Cloudinary",
                new RuntimeException("timeout"));

        ResponseEntity<Object> response = handler.handleCloudinaryIntegration(ex, crearRequest("/v1/imagenes"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(502);
        assertThat(body.get("error")).isEqualTo("Bad Gateway");
        assertThat(body.get("message")).isEqualTo("No fue posible procesar y optimizar la imagen con Cloudinary");
        assertThat(body.get("path")).isEqualTo("/v1/imagenes");
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleBadRequestRetorna400() {
        ResponseEntity<Object> response = handler.handleBadRequest(
                new IllegalArgumentException("Parámetro inválido"), crearRequest("/v1/casos"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("status", 400).containsEntry("message", "Parámetro inválido");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleMissingServletRequestPartRetorna400() {
        MissingServletRequestPartException ex = new MissingServletRequestPartException("documentoSoporte");

        ResponseEntity<Object> response = handler.handleMissingServletRequestPart(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, crearRequest("/v1/auth/registro/institucional"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("status", 400)
                .containsEntry("message", "Parte requerida faltante: documentoSoporte");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleAuthYAccessDeniedRetornan401Y403() {
        ResponseEntity<Object> unauth = handler.handleAuth(
                new BadCredentialsException("Credenciales inválidas"), crearRequest("/v1/auth/login"));
        assertThat(unauth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((Map<String, Object>) unauth.getBody()).containsEntry("status", 401);

        ResponseEntity<Object> forbidden = handler.handleAccessDenied(
                new AccessDeniedException("Solo fundaciones verificadas"), crearRequest("/v1/casos/1/custodia"));
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat((Map<String, Object>) forbidden.getBody())
                .containsEntry("status", 403)
                .containsEntry("message", "Solo fundaciones verificadas");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleNotFoundConflictYFallback500RetornanCodigosEsperados() {
        ResponseEntity<Object> notFound = handler.handleNotFound(
                new ResourceNotFoundException("Caso", 99L), crearRequest("/v1/casos/99"));
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Map<String, Object>) notFound.getBody()).containsEntry("status", 404);

        ResponseEntity<Object> conflict = handler.handleConflict(
                new ConflictException("NIT duplicado"), crearRequest("/v1/auth/registro"));
        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((Map<String, Object>) conflict.getBody()).containsEntry("status", 409);

        ResponseEntity<Object> fallback = handler.handleAll(
                new RuntimeException("Fallo inesperado"), crearRequest("/v1/casos"));
        assertThat(fallback.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat((Map<String, Object>) fallback.getBody())
                .containsEntry("status", 500)
                .containsEntry("message", "Error interno del servidor");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleMethodArgumentNotValidRetorna422ConErroresDeCampo() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
                new MockMultipartFile("f", new byte[0]), "registro");
        bindingResult.addError(new FieldError("registro", "correo", "Debe ser un correo válido"));
        var methodParam = new org.springframework.core.MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParam, bindingResult);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, crearRequest("/v1/auth/registro"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("status", 422);
        Map<String, String> errores = (Map<String, String>) body.get("errores");
        assertThat(errores).containsEntry("correo", "Debe ser un correo válido");
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String param) {
    }
}

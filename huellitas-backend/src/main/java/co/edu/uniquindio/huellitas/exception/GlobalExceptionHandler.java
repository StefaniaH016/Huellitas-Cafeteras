package co.edu.uniquindio.huellitas.exception;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.*;

/**
 * Manejador global de excepciones. Devuelve respuestas JSON consistentes
 * con la estructura: { timestamp, status, error, message, path }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ── Validación de @Valid ──────────────────────────────────────

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errores.put(campo, error.getDefaultMessage());
        });

        Map<String, Object> body = buildBody(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Errores de validación",
                request);
        body.put("errores", errores);

        return ResponseEntity.unprocessableEntity().body(body);
    }

    // ── 400 Bad Request & Multipart ──────────────────────────────

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            org.springframework.web.multipart.MultipartException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            org.springframework.web.multipart.support.MissingServletRequestPartException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, "Parte requerida faltante: " + ex.getRequestPartName(), request));
    }

    // ── 401 Autenticación ─────────────────────────────────────────

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuth(
            AuthenticationException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildBody(HttpStatus.UNAUTHORIZED, ex.getMessage(), request));
    }

    // ── 403 Autorización ──────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildBody(HttpStatus.FORBIDDEN, "Acceso denegado", request));
    }

    // ── 404 Recurso no encontrado ─────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), request));
    }

    // ── 409 Conflicto (ej: email duplicado) ──────────────────────

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleConflict(
            ConflictException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, ex.getMessage(), request));
    }

    // ── 500 Fallback ──────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        return ResponseEntity.internalServerError()
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error interno del servidor", request));
    }

    // ── Helper ────────────────────────────────────────────────────

    private Map<String, Object> buildBody(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return body;
    }
}

package appsembly.appsembly.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import appsembly.appsembly.service.ApplicationMetrics;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final ApplicationMetrics applicationMetrics;

    public GlobalExceptionHandler(ApplicationMetrics applicationMetrics) {
        this.applicationMetrics = applicationMetrics;
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception exception) {
        applicationMetrics.recordBadRequest();
        log.warn("Bad request: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "errorType", "BAD_REQUEST",
                "message", exception.getMessage() == null ? "Solicitud inválida." : exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException exception) {
        applicationMetrics.recordConflict();
        log.warn("Conflict: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "errorType", "CONFLICT",
                "message", exception.getMessage() == null ? "No se pudo completar la operación." : exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception) {
        applicationMetrics.recordUnexpectedError();
        log.error("Unexpected server error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "errorType", "INTERNAL_ERROR",
                "message", "Ocurrió un error inesperado al procesar la solicitud."));
    }
}
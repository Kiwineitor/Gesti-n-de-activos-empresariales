package com.sath.sistema_sath.Config.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaNegocio(ReglaDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "BUSINESS_RULE_VIOLATION",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarEstadoIlegal(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "ILLEGAL_STATE",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> manejarMetodoNoSoportado(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Método {} no soportado en URL: {} | Caller-IP: {}",
                ex.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Map.of(
                "error", "METHOD_NOT_ALLOWED",
                "method", ex.getMethod(),
                "path", request.getRequestURI()
        ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> manejarRecursoEstatico() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ClientAbortException.class)
    public void manejarClienteAbortado() {
        // cliente cerró la conexión antes de recibir la respuesta, ignorar
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGenerico(Exception ex, HttpServletRequest request) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        log.error("Error en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", ex.getMessage() != null ? ex.getMessage() : "Error inesperado",
                "path", request.getRequestURI()
        ));
    }
}

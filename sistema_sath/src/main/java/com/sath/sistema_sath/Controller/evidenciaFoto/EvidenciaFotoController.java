package com.sath.sistema_sath.Controller.evidenciaFoto;

import com.sath.sistema_sath.domain.evidenciaFoto.EvidenciaFotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evidencia")
@RequiredArgsConstructor
public class EvidenciaFotoController {

    private final EvidenciaFotoService service;

    @PostMapping(value = "/{idSolicitud}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> subir(
            @PathVariable Long idSolicitud,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "descripcion", required = false, defaultValue = "") String descripcion) {
        try {
            Map<String, Object> result = service.subir(idSolicitud, archivo, descripcion);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> listar(@PathVariable Long idSolicitud) {
        return ResponseEntity.ok(service.listar(idSolicitud));
    }

    @DeleteMapping("/{idEvidenciaFoto}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idEvidenciaFoto) {
        try {
            service.eliminar(idEvidenciaFoto);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/foto/{idEvidenciaFoto}")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long idEvidenciaFoto) {
        try {
            byte[] bytes = service.obtenerBytes(idEvidenciaFoto);
            String contentType = service.obtenerContentType(idEvidenciaFoto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
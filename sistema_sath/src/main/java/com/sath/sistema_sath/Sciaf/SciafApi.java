package com.sath.sistema_sath.Sciaf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sciaf")
public class SciafApi {
    private final SciafService sciafService;

    @PostMapping("/activo")
    public ResponseEntity<List<Map<String, Object>>> getHistorial(
            @RequestBody SciafRequestDTO request) {
        List<Map<String, Object>> resultado = sciafService
                .obtenerHistorialActivo(request.getCodigoActivo());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/ficha/{codigo}")
    public ResponseEntity<?> getFicha(@PathVariable String codigo) {
        Map<String, Object> ficha = sciafService.obtenerFichaActivo(codigo);
        if (ficha == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ficha);
    }   
}

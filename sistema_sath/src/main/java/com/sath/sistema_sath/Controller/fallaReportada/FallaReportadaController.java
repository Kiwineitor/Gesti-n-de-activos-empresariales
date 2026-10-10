package com.sath.sistema_sath.Controller.fallaReportada;

import com.sath.sistema_sath.domain.fallaReportada.FallaReportadaService;
import com.sath.sistema_sath.domain.fallaReportada.dto.CatalogarFallaDto;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaCreateDto;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaResponseDto;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/fallas-reportadas")
@RequiredArgsConstructor
public class FallaReportadaController {

    private final FallaReportadaService fallaReportadaService;



    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<FallaReportadaResponseDto> create(@Valid @RequestBody FallaReportadaCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fallaReportadaService.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<FallaReportadaResponseDto>> findAll() {
        return ResponseEntity.ok(fallaReportadaService.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<FallaReportadaResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(fallaReportadaService.findById(id));
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<FallaReportadaResponseDto> update(@PathVariable Long id, @Valid @RequestBody FallaReportadaUpdateDto dto) {
        return ResponseEntity.ok(fallaReportadaService.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fallaReportadaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/catalogar")
    @ResponseBody
    public ResponseEntity<FallaReportadaResponseDto> catalogar(
            @PathVariable Long id,
            @RequestBody CatalogarFallaDto dto) {
        return ResponseEntity.ok(fallaReportadaService.catalogarFalla(id, dto.idFalla()));
    }

    @GetMapping("/listaParaElFrontendFallaReportada")
    @ResponseBody
    public ResponseEntity<?> listaParaElFrontendFallaReportada() {
        try {
            List<Map<String, Object>> lista = fallaReportadaService.listaFallaReportada();
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            // Creamos un pequeño mapa para devolver un mensaje de error claro
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al recuperar la lista de activos");
            error.put("mensaje", e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // 500 Error

        }
    }

}
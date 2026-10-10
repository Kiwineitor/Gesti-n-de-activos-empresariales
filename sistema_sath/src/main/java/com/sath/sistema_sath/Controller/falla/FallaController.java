package com.sath.sistema_sath.Controller.falla;

import com.sath.sistema_sath.domain.falla.FallaService;
import com.sath.sistema_sath.domain.falla.dto.FallaCreateDto;
import com.sath.sistema_sath.domain.falla.dto.FallaMapper;
import com.sath.sistema_sath.domain.falla.dto.FallaResponseDto;
import com.sath.sistema_sath.domain.falla.dto.FallaUpdateDto;
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
@RequiredArgsConstructor
@RequestMapping("/api/falla")
public class FallaController {

    private final FallaService service;
    private final FallaMapper mapper;



    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<FallaResponseDto> create(@RequestBody FallaCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<FallaResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<FallaResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<FallaResponseDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody FallaUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));

    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-tipo/{idTipoActivo}")
    @ResponseBody
    public ResponseEntity<List<FallaResponseDto>> findByTipoActivo(@PathVariable Long idTipoActivo) {
        return ResponseEntity.ok(service.findByTipoActivo(idTipoActivo));
    }

    @GetMapping("listaParaElFrontendFAlla")
    @ResponseBody
    public ResponseEntity<?> listaParaElFrontendFAlla() {
        try {
            List<Map<String, Object>> lista = service.listaParaElFrontendFalla();
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al recuperar la lista de activos");
            error.put("mensaje", e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // 500 Error
        }
    }
}


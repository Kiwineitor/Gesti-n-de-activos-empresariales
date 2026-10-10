package com.sath.sistema_sath.Controller.permiso;

import com.sath.sistema_sath.domain.permiso.PermisoService;
import com.sath.sistema_sath.domain.permiso.dto.PermisoCreateDto;
import com.sath.sistema_sath.domain.permiso.dto.PermisoResponseDto;
import com.sath.sistema_sath.domain.permiso.dto.PermisoUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/permiso")
public class PermisoController {

    private final PermisoService service;



    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<PermisoResponseDto> create(@Valid @RequestBody PermisoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<PermisoResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PermisoUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<PermisoResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<PermisoResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaPermiso() {
        return service.listaPermiso();
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
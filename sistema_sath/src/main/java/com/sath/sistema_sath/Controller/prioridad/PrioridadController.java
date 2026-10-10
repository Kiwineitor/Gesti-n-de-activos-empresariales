package com.sath.sistema_sath.Controller.prioridad;

import com.sath.sistema_sath.domain.prioridad.PrioridadService;
import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;
import com.sath.sistema_sath.domain.prioridad.dto.PrioridadCreateDto;
import com.sath.sistema_sath.domain.prioridad.dto.PrioridadEstadoDto;
import com.sath.sistema_sath.domain.prioridad.dto.PrioridadresponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/prioridad")
@RequiredArgsConstructor
public class PrioridadController {

    private final PrioridadService service;



    @PostMapping
    @ResponseBody
    public ResponseEntity<PrioridadresponseDto> create(@Valid @RequestBody PrioridadCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<PrioridadresponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<PrioridadresponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaPrioridad() {
        return service.listaPrioridad();
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/prioridades")
    @ResponseBody
    public ResponseEntity<PrioridadEnum[]> listarPrioridades() {
        return ResponseEntity.ok(PrioridadEnum.values());
    }

    @PatchMapping("/{id}/nivel")
    @ResponseBody
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            @RequestBody PrioridadEstadoDto dto
            ){
        service.cambiarEstado(id, dto);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }

}

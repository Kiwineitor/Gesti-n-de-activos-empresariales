package com.sath.sistema_sath.Controller.tipoActivo;

import com.sath.sistema_sath.domain.tipoActivo.TipoActivoService;
import com.sath.sistema_sath.domain.tipoActivo.dto.TipoActivoCreateDto;
import com.sath.sistema_sath.domain.tipoActivo.dto.TipoActivoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/tipo-activo")
@RequiredArgsConstructor
public class TipoActivoController {

    private final TipoActivoService service;




    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<TipoActivoResponseDto> create(@RequestBody TipoActivoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @RequestMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<TipoActivoResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @RequestMapping("/{id}")
    @ResponseBody
    public ResponseEntity<TipoActivoResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @RequestMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaTipoActivo() {
        return service.listaTipoActivo();
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<TipoActivoResponseDto> update(@PathVariable Long id, @RequestBody TipoActivoCreateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}

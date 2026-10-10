package com.sath.sistema_sath.Controller.tipoServicio;

import com.sath.sistema_sath.domain.tipoServicio.TipoServicioService;
import com.sath.sistema_sath.domain.tipoServicio.dto.TipoServicioCreateDto;
import com.sath.sistema_sath.domain.tipoServicio.dto.TipoServicioResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/tipo-servicio")
public class TipoServicioController {

    private final TipoServicioService service;


    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> lista() {
        return service.listaTipoServicio();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<TipoServicioResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<TipoServicioResponseDto> create(@RequestBody TipoServicioCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<TipoServicioResponseDto> update(@PathVariable Long id,
                                                          @RequestBody TipoServicioCreateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

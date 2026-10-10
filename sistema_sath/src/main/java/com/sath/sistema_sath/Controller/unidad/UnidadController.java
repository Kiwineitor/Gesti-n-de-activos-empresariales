package com.sath.sistema_sath.Controller.unidad;

import com.sath.sistema_sath.domain.unidad.UnidadService;
import com.sath.sistema_sath.domain.unidad.dto.UnidadCreateDto;
import com.sath.sistema_sath.domain.unidad.dto.UnidadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/unidad")
public class UnidadController {

    private final UnidadService servicio;


    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<UnidadResponseDto> create(@RequestBody UnidadCreateDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<UnidadResponseDto>> findAll(){
        return ResponseEntity.ok(servicio.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<UnidadResponseDto> findById(@PathVariable Long id){
        return ResponseEntity.ok(servicio.findById(id));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaUnidad() {
        return servicio.listaUnidad();
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id){
        servicio.delete(id);
        return ResponseEntity.noContent().build();
    }

}

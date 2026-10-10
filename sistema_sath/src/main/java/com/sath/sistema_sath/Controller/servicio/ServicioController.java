package com.sath.sistema_sath.Controller.servicio;

import com.sath.sistema_sath.domain.servicio.ServicioService;
import com.sath.sistema_sath.domain.servicio.dto.ServicioCreateDto;
import com.sath.sistema_sath.domain.servicio.dto.ServicioResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/servicio")
public class ServicioController {

    private final ServicioService service;


    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<ServicioResponseDto> create(@Valid @RequestBody ServicioCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }



    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ServicioResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaServicio() {
        return service.listaServicio();
    }

    // Endpoint que faltaba — Alpine llama PUT /api/servicio/editar/{id}
    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<ServicioResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ServicioCreateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
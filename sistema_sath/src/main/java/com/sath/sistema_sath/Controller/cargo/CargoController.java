package com.sath.sistema_sath.Controller.cargo;

import com.sath.sistema_sath.domain.cargo.CargoService;
import com.sath.sistema_sath.domain.cargo.dto.CargoCreateDto;
import com.sath.sistema_sath.domain.cargo.dto.CargoResponseDto;
import com.sath.sistema_sath.domain.cargo.interfaz.CargoProjection;
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
@RequestMapping("/api/cargo")
public class CargoController {

    private final CargoService service;



    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<CargoResponseDto> create(@Valid @RequestBody CargoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<CargoResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<CargoResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }


    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listar-activos/{estado}")
    public ResponseEntity<List<CargoProjection>> findByEstado(@PathVariable String estado) {
        return ResponseEntity.ok(service.findByEstado(estado));
    }

    @GetMapping("/listaParaElFrontend")
    @ResponseBody
    public ResponseEntity<?> listaParaElFrontend() {
        try {
            List<Map<String, Object>> lista = service.listaParaElFrontend();
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al recuperar los datos del servidor");
            error.put("mensaje", e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // 500 Error
        }
    }

}

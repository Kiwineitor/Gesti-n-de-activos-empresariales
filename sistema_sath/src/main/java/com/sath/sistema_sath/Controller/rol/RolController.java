package com.sath.sistema_sath.Controller.rol;

import com.sath.sistema_sath.domain.rol.RolService;
import com.sath.sistema_sath.domain.rol.dto.RolCreateDto;
import com.sath.sistema_sath.domain.rol.dto.RolResponseDto;
import com.sath.sistema_sath.domain.rol.dto.RolUpdateDto;
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
@RequestMapping("/api/rol")
public class RolController {

    private final RolService service;

    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<RolResponseDto> create(@Valid @RequestBody RolCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<RolResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody RolUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<RolResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<RolResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaRol() {
        return service.listaRol();
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/varificar")
    public ResponseEntity <Map<String, Object>> varificarRol(@RequestParam (name = "nombreRol" ,required = false) String nombreRol){
        boolean existe =  service.tieneRol(nombreRol);

        Map<String, Object> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }
}

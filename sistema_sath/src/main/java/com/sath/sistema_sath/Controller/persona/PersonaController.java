package com.sath.sistema_sath.Controller.persona;

import com.sath.sistema_sath.domain.persona.PersonaService;
import com.sath.sistema_sath.domain.persona.dto.PersonaCreateDto;
import com.sath.sistema_sath.domain.persona.dto.PersonaResponseDto;
import com.sath.sistema_sath.domain.persona.dto.PersonaUpdateDto;
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
@RequestMapping("/api/persona")
public class PersonaController {

    private final PersonaService service;



    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<PersonaResponseDto> create(@Valid @RequestBody PersonaCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<PersonaResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<PersonaResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaPersona() {
        return service.listaPersona();
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<PersonaResponseDto> update(@PathVariable Long id, @Valid @RequestBody PersonaUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}

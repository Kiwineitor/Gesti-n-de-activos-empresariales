package com.sath.sistema_sath.Controller.tecnicoAsignado;

import com.sath.sistema_sath.domain.tecnicoAsignado.TecnicoAsignadoService;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoCreateDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoCreateMasivoDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoResponseDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/tecnicoAsignado")
public class TecnicoAsignadoController {

    private final TecnicoAsignadoService service;


    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<TecnicoAsignadoResponseDto> create(@Valid @RequestBody TecnicoAsignadoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PostMapping("/crear-masivo")
    @ResponseBody
    public ResponseEntity<List<TecnicoAsignadoResponseDto>> createMasivo(@RequestBody TecnicoAsignadoCreateMasivoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMasivo(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<TecnicoAsignadoResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<TecnicoAsignadoResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaTecnicoAsignado() {
        return service.listaTecnicoAsignado();
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<TecnicoAsignadoResponseDto> update(
            @PathVariable Long id,
            @RequestBody TecnicoAsignadoUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tomar/{idSolicitud}")
    @ResponseBody
    public ResponseEntity<TecnicoAsignadoResponseDto> tomar(
            @PathVariable Long idSolicitud, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.tomar(principal.getName(), idSolicitud));
    }
}

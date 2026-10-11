package com.sath.sistema_sath.Controller.activo;

import com.sath.sistema_sath.domain.activo.Activo;
import com.sath.sistema_sath.domain.activo.ActivoRepository;
import com.sath.sistema_sath.domain.activo.ActivoService;
import com.sath.sistema_sath.domain.activo.dto.ActivoCreateDto;
import com.sath.sistema_sath.domain.activo.dto.ActivoResponseDto;
import com.sath.sistema_sath.domain.activo.dto.ActivoUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/activos")
@RequiredArgsConstructor
public class ActivoController {

    private final ActivoService activoService;
    private final ActivoRepository activoRepo;


    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<ActivoResponseDto> create(@Valid @RequestBody ActivoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activoService.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<ActivoResponseDto>> findAll() {
        return ResponseEntity.ok(activoService.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ActivoResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(activoService.findById(id));
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<ActivoResponseDto> update(@PathVariable Long id, @Valid @RequestBody ActivoUpdateDto dto) {
        return ResponseEntity.ok(activoService.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filtrados")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getActivosFiltrados() {
        List<Map<String, Object>> lista = activoService.listarActivosParaFrontend();
        return ResponseEntity.ok(lista);
    }


    @GetMapping("/historial")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> historial(@RequestParam String codigo) {
        return ResponseEntity.ok(activoService.historialPorCodigoActivo(codigo.trim()));
    }

    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarPorCodigo(@RequestParam String codigo) {
        Optional<Activo> opt = activoRepo.findByCodigoAct(codigo.trim());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No se encontró recurso con código: " + codigo.trim()));
        }
        Activo a = opt.get();
        return ResponseEntity.ok(Map.of(
                "idActivo", a.getIdActivo(),
                "codigoActivo", a.getCodigoAct(),
                "descripcionActivo", a.getDescripcionAct() != null ? a.getDescripcionAct() : "",
                "tipoActivo", a.getTipoActivo() != null ? a.getTipoActivo().getNombreTipoAct() : ""
        ));
    }
}
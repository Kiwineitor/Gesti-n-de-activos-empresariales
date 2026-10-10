package com.sath.sistema_sath.Controller.Administrativo;

import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.administrativo.AdministrativoService;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoCreateDto;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoResponseDto;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/administrativo")
public class AdministrativoController {

    private final AdministrativoService service;
    private final AdministrativoRepository repo;


    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<AdministrativoResponseDto> create(@Valid @RequestBody AdministrativoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<AdministrativoResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<AdministrativoResponseDto> findById(@PathVariable Long id) {
        log.info(">>> findById llamado con id={}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<AdministrativoResponseDto> update(@PathVariable Long id,
                                                            @Valid @RequestBody AdministrativoUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarPorCi(@RequestParam String ci) {
        String valor = ci.trim();

        // Intento 1: buscar por CI
        List<Map<String, Object>> resultado = repo.findByCiPersona(valor);
        if (!resultado.isEmpty()) return ResponseEntity.ok(resultado.get(0));

        // Intento 2: si es numérico, buscar por código de funcionario
        try {
            long codigo = Long.parseLong(valor);
            List<Map<String, Object>> porCodigo = repo.findByCodigoAdmiMap(codigo);
            if (!porCodigo.isEmpty()) return ResponseEntity.ok(porCodigo.get(0));
        } catch (NumberFormatException e) {
            // no es numérico, continuar
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("mensaje", "No se encontró funcionario con CI o código: " + valor));
    }

    @GetMapping("/buscar-por-codigo")
    @ResponseBody
    public ResponseEntity<?> buscarPorCodigo(@RequestParam Long codigo) {
        List<Map<String, Object>> resultado = repo.findByCodigoAdmiMap(codigo);
        if (!resultado.isEmpty()) return ResponseEntity.ok(resultado.get(0));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("mensaje", "No se encontró funcionario con código: " + codigo));
    }

    @GetMapping("/listaParaElFrontend")
    @ResponseBody
    public ResponseEntity<?> listaParaElFrontend() {
        try {
            List<Map<String, Object>> lista = service.listaParaElFrontend();

            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 No Content
            }

            return ResponseEntity.ok(lista);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al recuperar la lista de activos");
            error.put("mensaje", e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // 500 Error
        }
    }
}

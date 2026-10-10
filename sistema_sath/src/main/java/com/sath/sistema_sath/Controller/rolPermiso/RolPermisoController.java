package com.sath.sistema_sath.Controller.rolPermiso;

import com.sath.sistema_sath.domain.rolPermiso.RolPermisoService;
import com.sath.sistema_sath.domain.rolPermiso.dto.AsignarPermisoDto;
import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoResponseDto;
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
@RequestMapping("/api/rol-permiso")
public class RolPermisoController {

    private final RolPermisoService service;



    @PostMapping("/asignar")
    @ResponseBody
    public ResponseEntity<RolPermisoResponseDto> asignar(@Valid @RequestBody AsignarPermisoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.asignarPermiso(dto));
    }

    @DeleteMapping("/revocar/{id}")
    @ResponseBody
    public ResponseEntity<Void> revocar(@PathVariable Long id) {
        service.revocarPermiso(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-rol/{idRol}")
    @ResponseBody
    public ResponseEntity<List<RolPermisoResponseDto>> findByRol(@PathVariable Long idRol) {
        return ResponseEntity.ok(service.findPermisosByRol(idRol));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaRolPermiso() {
        return service.listaRolPermiso();
    }
}
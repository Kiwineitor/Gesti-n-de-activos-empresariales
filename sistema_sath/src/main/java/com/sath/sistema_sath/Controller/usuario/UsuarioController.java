package com.sath.sistema_sath.Controller.usuario;

import com.sath.sistema_sath.domain.usuario.UsuarioService;
import com.sath.sistema_sath.domain.usuario.dto.*;
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
@RequestMapping("/api/usuario")
public class UsuarioController {

    private final UsuarioService service;


    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<UsuarioResponseDto> create(@Valid @RequestBody UsuarioCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PostMapping("/crear-completo")
    @ResponseBody
    public ResponseEntity<UsuarioResponseDto> crearCompleto(@Valid @RequestBody UsuarioCompletoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearCompleto(dto));
    }

    @ResponseBody
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<UsuarioResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @ResponseBody
    @PatchMapping("/estado/{id}")
    public ResponseEntity<UsuarioResponseDto> changeEstado(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioChangeEstadoDto dto) {
        return ResponseEntity.ok(service.changeEstado(id, dto));
    }

    @ResponseBody
    @PatchMapping("/toggle-estado/{id}")
    public ResponseEntity<UsuarioResponseDto> toggleEstado(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleEstado(id));
    }

    @ResponseBody
    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @ResponseBody
    @GetMapping("/con-administrativo")
    public ResponseEntity<List<Map<String, Object>>> usuariosConAdministrativo() {
        return ResponseEntity.ok(service.usuariosConAdministrativo());
    }

    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @ResponseBody
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @PatchMapping("/reset-password/{id}")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordAdminDto dto) {
        service.resetPassword(id, dto);
        return ResponseEntity.noContent().build();
    }
}

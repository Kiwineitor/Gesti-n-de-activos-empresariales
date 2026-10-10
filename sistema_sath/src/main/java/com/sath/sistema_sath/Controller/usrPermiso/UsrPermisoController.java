package com.sath.sistema_sath.Controller.usrPermiso;

import com.sath.sistema_sath.domain.usrPermiso.UsrPermisoService;
import com.sath.sistema_sath.domain.usrPermiso.dto.AsignarPermisoUsuarioDto;
import com.sath.sistema_sath.domain.usrPermiso.dto.UsrPermisoResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usr-permiso")
public class UsrPermisoController {

    private final UsrPermisoService service;

    @PostMapping("/asignar")
    public ResponseEntity<UsrPermisoResponseDto> asignar(@Valid @RequestBody AsignarPermisoUsuarioDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.asignar(dto));
    }

    @DeleteMapping("/revocar/{id}")
    public ResponseEntity<Void> revocar(@PathVariable Long id) {
        service.revocar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-usuario/{idUsuario}")
    public ResponseEntity<List<UsrPermisoResponseDto>> porUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.findByUsuario(idUsuario));
    }
}

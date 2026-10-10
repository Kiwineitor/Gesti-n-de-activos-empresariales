package com.sath.sistema_sath.Controller.usuario;

import com.sath.sistema_sath.domain.persona.dto.PersonaResponseDto;
import com.sath.sistema_sath.domain.usuario.PerfilService;
import com.sath.sistema_sath.domain.usuario.dto.CambiarPasswordDto;
import com.sath.sistema_sath.domain.usuario.dto.CambiarUsernameDto;
import com.sath.sistema_sath.domain.usuario.dto.UsuarioResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService service;

    // GET /api/perfil/me  → datos del usuario logueado
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> getMiPerfil(Principal principal) {
        return ResponseEntity.ok(service.getMiPerfil(principal.getName()));
    }

    // PATCH /api/perfil/username  → cambiar nombre de usuario
    @PatchMapping("/username")
    public ResponseEntity<UsuarioResponseDto> cambiarUsername(
            Principal principal,
            @Valid @RequestBody CambiarUsernameDto dto) {
        UsuarioResponseDto updated = service.cambiarUsername(principal.getName(), dto);
        return ResponseEntity.ok(updated);
    }

    // PATCH /api/perfil/password  → cambiar contraseña
    @PatchMapping("/password")
    public ResponseEntity<Void> cambiarPassword(
            Principal principal,
            @Valid @RequestBody CambiarPasswordDto dto) {
        service.cambiarPassword(principal.getName(), dto);
        return ResponseEntity.noContent().build();
    }

    // GET /api/perfil/persona  → datos de la persona vinculada al usuario logueado
    @GetMapping("/persona")
    public ResponseEntity<PersonaResponseDto> getMiPersona(Principal principal) {
        PersonaResponseDto persona = service.getMiPersona(principal.getName());
        if (persona == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(persona);
    }
}
package com.sath.sistema_sath.Controller.auth;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.Config.security.CustomUserDetailsService;
import com.sath.sistema_sath.Config.security.JwtService;
import com.sath.sistema_sath.Config.security.dto.LoginRequestDto;
import com.sath.sistema_sath.Config.security.dto.LoginResponseDto;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final AdministrativoRepository adminRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto dto) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Usuario o contraseña incorrectos"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Error de autenticación: " + e.getMessage()));
        }
        Usuario usuario = usuarioRepository.findByUsername(dto.username()).orElse(null);
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.username());

        if (usuario == null || !EstadoEnum.ACTIVO.equals(usuario.getEstado())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Usuario inactivo o suspendido"));
        }

        Long idUsuario = usuario != null ? usuario.getIdUsuario() : 0L;
        String token = jwtService.generateToken(userDetails, idUsuario);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .toList();

        // Permisos de módulo (y cualquier otro permiso) asignados via RolPermiso
        List<String> permisos = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .toList();

        String nombreCompleto = (usuario != null && usuario.getPersona() != null)
                ? usuario.getPersona().getNombreCompleto()
                : dto.username();

        String cargo = null;
        if (usuario != null && usuario.getPersona() != null) {
            cargo = adminRepo.findFirstByPersona(usuario.getPersona())
                    .map(a -> a.getCargo() != null ? a.getCargo().getNombreCar() : null)
                    .orElse(null);
        }

        return ResponseEntity.ok(new LoginResponseDto(token, dto.username(), nombreCompleto, roles, cargo, permisos));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Token requerido"));
        }
        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Token inválido"));
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(token, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Token expirado o inválido"));
        }
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null || !EstadoEnum.ACTIVO.equals(usuario.getEstado())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Usuario inactivo"));
        }
        String newToken = jwtService.generateToken(userDetails, usuario.getIdUsuario());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .toList();
        List<String> permisos = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .toList();
        String nombreCompleto = (usuario.getPersona() != null) ? usuario.getPersona().getNombreCompleto() : username;
        String cargo = null;
        if (usuario.getPersona() != null) {
            cargo = adminRepo.findFirstByPersona(usuario.getPersona())
                    .map(a -> a.getCargo() != null ? a.getCargo().getNombreCar() : null)
                    .orElse(null);
        }
        return ResponseEntity.ok(new LoginResponseDto(newToken, username, nombreCompleto, roles, cargo, permisos));
    }

    record ErrorResponse(String error) {
    }
}

package com.sath.sistema_sath.domain.usuario;

import com.sath.sistema_sath.Config.Exception.RecursoNoEncontradoException;
import com.sath.sistema_sath.Config.Exception.ReglaDeNegocioException;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.persona.Persona;
import com.sath.sistema_sath.domain.persona.dto.PersonaMapper;
import com.sath.sistema_sath.domain.persona.dto.PersonaResponseDto;
import com.sath.sistema_sath.domain.usuario.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository repo;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AdministrativoRepository admRepo;
    private final PersonaMapper personaMapper;

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto getMiPerfil(String username) {
        Usuario usuario = repo.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return enrich(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDto cambiarUsername(String usernameActual, CambiarUsernameDto dto) {
        Usuario usuario = repo.findByUsername(usernameActual)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (repo.existsByUsername(dto.getUsernameNuevo())) {
            throw new ReglaDeNegocioException("El nombre de usuario ya está en uso");
        }

        usuario.setUsername(dto.getUsernameNuevo());
        return enrich(repo.save(usuario));
    }

    @Override
    @Transactional
    public void cambiarPassword(String username, CambiarPasswordDto dto) {
        Usuario usuario = repo.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new ReglaDeNegocioException("La contraseña actual es incorrecta");
        }

        // Verificar que las nuevas coincidan
        if (!dto.getPasswordNueva().equals(dto.getConfirmarPassword())) {
            throw new ReglaDeNegocioException("Las contraseñas nuevas no coinciden");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));
        repo.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponseDto getMiPersona(String username) {
        Usuario usuario = repo.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Persona persona = usuario.getPersona();
        if (persona == null) return null;
        return personaMapper.toResponseDto(persona);
    }

    private UsuarioResponseDto enrich(Usuario u) {
        UsuarioResponseDto base = mapper.toResponseDto(u);
        List<String> roles = mapper.extractRoles(u.getRoles());
        Long idAdministrativo = null;
        if (u.getPersona() != null) {
            Administrativo adm = admRepo.findByPersona(u.getPersona());
            if (adm != null) idAdministrativo = adm.getIdAdministrativo();
        }
        return new UsuarioResponseDto(
                base.idUsuario(), base.username(), base.idPersona(),
                base.nombrePersona(), base.estado(), roles, null, idAdministrativo
        );
    }
}
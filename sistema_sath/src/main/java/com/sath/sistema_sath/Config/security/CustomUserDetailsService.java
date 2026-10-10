package com.sath.sistema_sath.Config.security;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.rolPermiso.RolPermisoRepository;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import com.sath.sistema_sath.domain.usrPermiso.UsrPermisoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final UsrPermisoRepository usrPermisoRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido — usuario no encontrado: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        // Roles como ROLE_ADMIN, ROLE_TECNICO...
        Stream<SimpleGrantedAuthority> roles = usuario.getRoles().stream()
                .filter(ur -> ur.getEstado() != EstadoEnum.ELIMINADO)
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRol().getNombreRol().toUpperCase()));

        // Permisos del rol (incluye MODULO_X si se asignan via RolPermiso)
        Stream<SimpleGrantedAuthority> permisosPorRol = usuario.getRoles().stream()
                .filter(ur -> ur.getEstado() != EstadoEnum.ELIMINADO)
                .flatMap(ur -> rolPermisoRepository
                        .findActiveByRolId(ur.getRol().getIdRol(), EstadoEnum.ELIMINADO).stream())
                .map(rp -> new SimpleGrantedAuthority(rp.getPermiso().getNombrePermiso()));

        // Permisos asignados directamente al usuario (UsrPermiso)
        Stream<SimpleGrantedAuthority> permisosPorUsuario = usrPermisoRepository
                .findActivosByUsername(username, EstadoEnum.ELIMINADO)
                .stream()
                .map(up -> new SimpleGrantedAuthority(up.getPermiso().getNombrePermiso()));

        List<SimpleGrantedAuthority> authorities = Stream.concat(roles, Stream.concat(permisosPorRol, permisosPorUsuario))
                .distinct()
                .collect(Collectors.toList());

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                authorities
        );
    }
}

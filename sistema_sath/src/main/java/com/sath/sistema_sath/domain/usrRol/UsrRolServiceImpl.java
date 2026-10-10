package com.sath.sistema_sath.domain.usrRol;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.rol.Rol;
import com.sath.sistema_sath.domain.rol.RolRepository;
import com.sath.sistema_sath.domain.rolPermiso.RolPermisoRepository;
import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoMapper;
import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoResponseDto;
import com.sath.sistema_sath.domain.usrRol.dto.AsignarRolDto;
import com.sath.sistema_sath.domain.usrRol.dto.UsrRolDto;
import com.sath.sistema_sath.domain.usrRol.dto.UsrRolMapper;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsrRolServiceImpl implements UsrRolService {

    private final UsrRolRepository usrRolRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final UsrRolMapper usrRolMapper;
    private final RolPermisoMapper rolPermisoMapper;

    @Override
    @Transactional
    public UsrRolDto asignarRol(AsignarRolDto dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.getIdUsuario()));
        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + dto.getIdRol()));

        // Verificar si ya está activo
        boolean yaActivo = usuario.getRoles().stream()
                .anyMatch(ur -> ur.getRol().getIdRol().equals(dto.getIdRol())
                        && ur.getEstado() != EstadoEnum.ELIMINADO);
        if (yaActivo) {
            throw new IllegalStateException("El usuario ya tiene asignado ese rol.");
        }

        // ✅ Reutilizar registro ELIMINADO en lugar de crear uno nuevo
        UsrRol usrRol = usuario.getRoles().stream()
                .filter(ur -> ur.getRol().getIdRol().equals(dto.getIdRol())
                        && ur.getEstado() == EstadoEnum.ELIMINADO)
                .findFirst()
                .orElse(new UsrRol()); // solo crea nuevo si no existe ninguno previo

        usrRol.setUsuario(usuario);
        usrRol.setRol(rol);
        usrRol.setEstado(EstadoEnum.ACTIVO);

        return usrRolMapper.toDto(usrRolRepository.save(usrRol));
    }

    @Override
    @Transactional
    public void revocarRol(Long idUsrRol) {
        UsrRol usrRol = usrRolRepository.findById(idUsrRol)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada: " + idUsrRol));
        usrRol.setEstado(EstadoEnum.ELIMINADO); // ✅ ya lo tenías bien
        usrRolRepository.save(usrRol);
    }

    @Override
    public List<Map<String, Object>> listaUsrRol() {
        return usrRolRepository.listaUsrRol();
    }

    @Override
    public List<UsrRolDto> findRolesByUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));

        return usuario.getRoles().stream()
                .filter(ur -> ur.getEstado() != EstadoEnum.ELIMINADO) // ✅
                .map(usrRolMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RolPermisoResponseDto> findPermisosByUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));

        return usuario.getRoles().stream()
                .filter(ur -> ur.getEstado() != EstadoEnum.ELIMINADO) // ✅
                .flatMap(ur -> rolPermisoRepository
                        .findActiveByRolId(ur.getRol().getIdRol(), EstadoEnum.ELIMINADO).stream()) // ✅
                .map(rolPermisoMapper::toResponseDto)
                .distinct()
                .collect(Collectors.toList());
    }
}
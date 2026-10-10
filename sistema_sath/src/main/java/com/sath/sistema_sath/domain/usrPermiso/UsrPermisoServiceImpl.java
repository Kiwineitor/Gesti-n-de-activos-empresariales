package com.sath.sistema_sath.domain.usrPermiso;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.permiso.Permiso;
import com.sath.sistema_sath.domain.permiso.PermisoRepository;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import com.sath.sistema_sath.domain.usrPermiso.dto.AsignarPermisoUsuarioDto;
import com.sath.sistema_sath.domain.usrPermiso.dto.UsrPermisoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsrPermisoServiceImpl implements UsrPermisoService {

    private final UsrPermisoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;

    @Override
    @Transactional
    public UsrPermisoResponseDto asignar(AsignarPermisoUsuarioDto dto) {
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.idUsuario()));

        Permiso permiso = permisoRepository.findById(dto.idPermiso())
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado: " + dto.idPermiso()));

        boolean yaAsignado = repository.existsByUsuario_IdUsuarioAndPermiso_IdPermisoAndEstadoNot(
                dto.idUsuario(), dto.idPermiso(), EstadoEnum.ELIMINADO);
        if (yaAsignado) {
            throw new IllegalStateException("El usuario ya tiene asignado ese permiso.");
        }

        UsrPermiso entity = new UsrPermiso();
        entity.setUsuario(usuario);
        entity.setPermiso(permiso);
        entity.setEstado(EstadoEnum.ACTIVO);

        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void revocar(Long idUsrPermiso) {
        UsrPermiso entity = repository.findById(idUsrPermiso)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada: " + idUsrPermiso));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repository.save(entity);
    }

    @Override
    public List<UsrPermisoResponseDto> findByUsuario(Long idUsuario) {
        return repository.findActivosByUsuario(idUsuario, EstadoEnum.ELIMINADO)
                .stream().map(this::toDto).toList();
    }

    private UsrPermisoResponseDto toDto(UsrPermiso e) {
        return new UsrPermisoResponseDto(
                e.getIdUsrPermiso(),
                e.getUsuario().getIdUsuario(),
                e.getUsuario().getUsername(),
                e.getPermiso().getIdPermiso(),
                e.getPermiso().getNombrePermiso(),
                e.getPermiso().getDescripcionPermiso(),
                e.getEstado() != null ? e.getEstado().name() : null
        );
    }
}

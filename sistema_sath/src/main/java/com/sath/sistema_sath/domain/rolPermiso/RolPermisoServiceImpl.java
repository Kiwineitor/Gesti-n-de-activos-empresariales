package com.sath.sistema_sath.domain.rolPermiso;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.permiso.Permiso;
import com.sath.sistema_sath.domain.permiso.PermisoRepository;
import com.sath.sistema_sath.domain.rol.Rol;
import com.sath.sistema_sath.domain.rol.RolRepository;
import com.sath.sistema_sath.domain.rolPermiso.dto.AsignarPermisoDto;
import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoMapper;
import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RolPermisoServiceImpl implements RolPermisoService {

    private final RolPermisoRepository rolPermisoRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoMapper mapper;

    @Override
    @Transactional
    public RolPermisoResponseDto asignarPermiso(AsignarPermisoDto dto) {
        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + dto.getIdRol()));

        Permiso permiso = permisoRepository.findById(dto.getIdPermiso())
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado: " + dto.getIdPermiso()));

        boolean yaAsignado = rolPermisoRepository
                .existsByRol_IdRolAndPermiso_IdPermisoAndEstadoNot(dto.getIdRol(), dto.getIdPermiso(), EstadoEnum.ELIMINADO);
        if (yaAsignado) {
            throw new IllegalStateException("El rol ya tiene asignado ese permiso.");
        }

        RolPermiso rolPermiso = new RolPermiso();
        rolPermiso.setRol(rol);
        rolPermiso.setPermiso(permiso);
        rolPermiso.setEstado(EstadoEnum.ACTIVO);

        return mapper.toResponseDto(rolPermisoRepository.save(rolPermiso));
    }

    @Override
    @Transactional
    public void revocarPermiso(Long idRolPermiso) {
        RolPermiso entity = rolPermisoRepository.findById(idRolPermiso)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada: " + idRolPermiso));
        entity.setEstado(EstadoEnum.ELIMINADO);
        rolPermisoRepository.save(entity);
    }

    @Override
    public List<Map<String, Object>> listaRolPermiso() {
        return rolPermisoRepository.listaRolPermiso();
    }

    @Override
    public List<RolPermisoResponseDto> findPermisosByRol(Long idRol) {
        return mapper.toResponseDtoList(rolPermisoRepository.findActiveByRolId(idRol, EstadoEnum.ELIMINADO));
    }
}

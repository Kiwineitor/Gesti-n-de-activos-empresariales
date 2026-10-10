package com.sath.sistema_sath.domain.permiso;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.permiso.dto.PermisoCreateDto;
import com.sath.sistema_sath.domain.permiso.dto.PermisoMapper;
import com.sath.sistema_sath.domain.permiso.dto.PermisoResponseDto;
import com.sath.sistema_sath.domain.permiso.dto.PermisoUpdateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;
    private final PermisoMapper mapper;

    @Override
    public PermisoResponseDto create(PermisoCreateDto dto) {
        if (permisoRepository.existsByNombrePermiso(dto.getNombrePermiso())) {
            throw new IllegalArgumentException("Ya existe un permiso con ese nombre: " + dto.getNombrePermiso());
        }
        return mapper.toResponseDto(permisoRepository.save(mapper.toEntity(dto)));
    }

    @Override
    @Transactional
    public PermisoResponseDto update(Long id, PermisoUpdateDto dto) {
        Permiso entity = permisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado: " + id));
        entity.setNombrePermiso(dto.getNombrePermiso());
        entity.setDescripcionPermiso(dto.getDescripcionPermiso());
        return mapper.toResponseDto(permisoRepository.save(entity));
    }

    @Override
    public List<PermisoResponseDto> findAll() {
        return mapper.toResponseDtoList(permisoRepository.findAll());
    }

    @Override
    public PermisoResponseDto findById(Long id) {
        return mapper.toResponseDto(permisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado: " + id)));
    }

    @Override
    public List<Map<String, Object>> listaPermiso() {
        return permisoRepository.listaPermiso();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Permiso entity = permisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        permisoRepository.save(entity);
    }
}

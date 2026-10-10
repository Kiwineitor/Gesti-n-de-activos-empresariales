package com.sath.sistema_sath.domain.activo;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.activo.dto.ActivoCreateDto;
import com.sath.sistema_sath.domain.activo.dto.ActivoMapper;
import com.sath.sistema_sath.domain.activo.dto.ActivoResponseDto;
import com.sath.sistema_sath.domain.activo.dto.ActivoUpdateDto;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivo;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ActivoServiceImpl implements ActivoService {
    private final ActivoRepository activoRepository;
    private final TipoActivoRepository tipoActivoRepository;
    private final ActivoMapper activoMapper;

    @Override
    public ActivoResponseDto create(ActivoCreateDto dto) {
        // 1. DTO → Entidad (sin relaciones)
        Activo entity = activoMapper.toEntity(dto);

        // 2. Reconstruir relaciones manualmente
        if (dto.getIdTipoActivo() != null) {
            TipoActivo tipo = tipoActivoRepository.findById(dto.getIdTipoActivo())
                    .orElseThrow(() -> new IllegalArgumentException("TipoActivo no encontrado con ID: " + dto.getIdTipoActivo()));
            entity.setTipoActivo(tipo);
        }

        // 3. Guardar y retornar DTO actualizado
        return activoMapper.toResponseDto(activoRepository.save(entity));
    }

    @Override
    public List<ActivoResponseDto> findAll() {
        return activoMapper.toResponseDtoList(activoRepository.findAll());
    }

    @Override
    public ActivoResponseDto findById(Long id) {
        Activo entity = activoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activo no encontrado con ID: " + id));
        return activoMapper.toResponseDto(entity);
    }

    @Override
    public ActivoResponseDto update(Long id, ActivoUpdateDto dto) {
        Activo entity = activoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activo no encontrado con ID: " + id));

        // 1. Usar el mapper para actualizar campos simples (código, descripción, etc.)
        activoMapper.updateFromDto(dto, entity);

        // 2. Actualizar relación manualmente (esto está perfecto como lo hiciste)
        if (dto.getIdTipoActivo() != null) {
            TipoActivo tipo = tipoActivoRepository.findById(dto.getIdTipoActivo())
                    .orElseThrow(() -> new IllegalArgumentException("TipoActivo no encontrado con ID: " + dto.getIdTipoActivo()));
            entity.setTipoActivo(tipo);
        }

        return activoMapper.toResponseDto(activoRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        Activo entity = activoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID no encontrado"));

        entity.setEstado(EstadoEnum.ELIMINADO);
        activoRepository.save(entity);
    }

    @Override
    public List<Map<String, Object>> listarActivosParaFrontend() {
        return activoRepository.listarActivosParaFrontend();
    }

    @Override
    public List<Map<String, Object>> historialPorCodigoActivo(String codigoActivo) {
        return activoRepository.historialPorCodigoActivo(codigoActivo);
    }
}

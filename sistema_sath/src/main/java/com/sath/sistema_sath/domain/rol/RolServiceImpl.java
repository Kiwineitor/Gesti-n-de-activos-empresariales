package com.sath.sistema_sath.domain.rol;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.rol.dto.RolCreateDto;
import com.sath.sistema_sath.domain.rol.dto.RolMapper;
import com.sath.sistema_sath.domain.rol.dto.RolResponseDto;
import com.sath.sistema_sath.domain.rol.dto.RolUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService{

    private final RolRepository rolRepository;
    private final RolMapper mapper;


    @Override
    public RolResponseDto create(RolCreateDto dto) {
        Rol entity = mapper.toEntity(dto);
        return mapper.toResponseDto(rolRepository.save(entity));
    }

    @Override
    public RolResponseDto update(Long id, RolUpdateDto dto) {
        Rol entity = rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
        entity.setNombreRol(dto.getNombreRol());
        entity.setDescripcionRol(dto.getDescripcionRol());
        return mapper.toResponseDto(rolRepository.save(entity));
    }

    @Override
    public List<RolResponseDto> findAll() {
        return mapper.toResponseDtoList(rolRepository.findAll());
    }

    @Override
    public RolResponseDto findById(Long id) {
        Rol entity = rolRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + id));

        return mapper.toResponseDto(entity);
    }

    @Override
    public List<Map<String, Object>> listaRol() {
        return rolRepository.listaRol();
    }

    @Override
    public void delete(Long id) {
        Rol entity = rolRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        rolRepository.save(entity);
    }

    @Override
    public boolean tieneRol(String tieneRol) {

        if (tieneRol == null || tieneRol.isBlank()){
            return false;
        }
        boolean existeRol = rolRepository.tieneRol(tieneRol);

        if (!existeRol){
            return false;
        }
        return true;
    }
}

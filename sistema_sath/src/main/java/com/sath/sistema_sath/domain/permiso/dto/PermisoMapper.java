package com.sath.sistema_sath.domain.permiso.dto;

import com.sath.sistema_sath.domain.permiso.Permiso;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermisoMapper {
    Permiso toEntity(PermisoCreateDto dto);

    PermisoResponseDto toResponseDto(Permiso entity);

    List<PermisoResponseDto> toResponseDtoList(List<Permiso> entities);
}

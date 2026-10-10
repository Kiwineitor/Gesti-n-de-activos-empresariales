package com.sath.sistema_sath.domain.rol.dto;

import com.sath.sistema_sath.domain.rol.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolMapper {

    Rol toEntity(RolCreateDto dto);
    RolResponseDto toResponseDto(Rol entity);
    List<RolResponseDto> toResponseDtoList(List<Rol> entities);
}

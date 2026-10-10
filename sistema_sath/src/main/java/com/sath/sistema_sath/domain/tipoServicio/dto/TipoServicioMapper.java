package com.sath.sistema_sath.domain.tipoServicio.dto;

import com.sath.sistema_sath.domain.tipoServicio.TipoServicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoServicioMapper {

    @Mapping(target = "idTipoServicio", ignore = true)
    TipoServicio toEntity(TipoServicioCreateDto dto);

    TipoServicioResponseDto toResponseDto(TipoServicio entity);
}

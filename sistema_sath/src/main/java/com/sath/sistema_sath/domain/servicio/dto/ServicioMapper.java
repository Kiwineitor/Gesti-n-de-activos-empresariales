package com.sath.sistema_sath.domain.servicio.dto;

import com.sath.sistema_sath.domain.servicio.Servicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServicioMapper {

    @Mapping(target = "idServicio", ignore = true)
    @Mapping(target = "tipoServicio", ignore = true)
    Servicio toEntity(ServicioCreateDto dto);

    @Mapping(source = "tipoServicio.idTipoServicio", target = "idTipoServicio")
    @Mapping(source = "tipoServicio.nombreTipo",     target = "nombreTipoServicio")
    ServicioResponseDto toResponseDto(Servicio entity);

    @Mapping(source = "tipoServicio.idTipoServicio", target = "idTipoServicio")
    @Mapping(source = "tipoServicio.nombreTipo",     target = "nombreTipoServicio")
    List<ServicioResponseDto> toResponseDtoList(List<Servicio> entities);
}

package com.sath.sistema_sath.domain.solicitud.dto;

import com.sath.sistema_sath.domain.solicitud.Solicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SolicitudMapper {

    @Mapping(target = "administrativo", ignore = true)
    @Mapping(target = "idSolicitud",    ignore = true)
    Solicitud toEntity(SolicitudCreateDto dto);

    @Mapping(target = "idSolicitud", ignore = true)
    Solicitud updateEntityFromDto(SolicitudUpdateDto dto, @MappingTarget Solicitud entity);

    @Mapping(target = "mantIdLondra", source = "mantIdLondra")
    SolicitudResponseDto toResponseDto(Solicitud entity);

    List<SolicitudResponseDto> toResponseDtoList(List<Solicitud> entities);
}

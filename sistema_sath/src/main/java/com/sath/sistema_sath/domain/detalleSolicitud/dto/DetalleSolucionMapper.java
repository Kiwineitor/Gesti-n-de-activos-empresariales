package com.sath.sistema_sath.domain.detalleSolicitud.dto;

import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetalleSolucionMapper {

    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "solicitud", ignore = true)
    DetalleSolicitud toEntity(DetalleSolicitudCreateDto dto);


    @Mapping(target = "idDetalleSolicitud", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "solicitud", ignore = true)
    DetalleSolicitud updateEntityFromDto(DetalleSolicitudUpdateDto dto, @MappingTarget DetalleSolicitud entity);

    @Mapping(source = "activo.idActivo", target = "idActivo")
    @Mapping(source = "activo.codigoAct", target = "codActivo")
    @Mapping(source = "solicitud.idSolicitud", target = "idSolicitud")
    @Mapping(source = "solicitud.nroSol", target = "nroSolicitud")
    DetalleSolicitudResponseDto toResponseDto(DetalleSolicitud entity);

    List<DetalleSolicitudResponseDto> toResponseDtoList(List<DetalleSolicitud> entities);
}

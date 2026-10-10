package com.sath.sistema_sath.domain.atencionSolicitud.dto;

import com.sath.sistema_sath.domain.atencionSolicitud.AtencionServicio;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionSolicitud;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AtencionSolicitudMapper {

    @Mapping(target = "atencionServicios", ignore = true)
    @Mapping(target = "prioridad", ignore = true)
    @Mapping(target = "detalleSolicitud", ignore = true)
    AtencionSolicitud toEntity(AtencionSolicitudCreateDto dto);

    @Mapping(target = "idAtencionSolicitud", ignore = true)
    @Mapping(target = "atencionServicios", ignore = true)
    @Mapping(target = "prioridad", ignore = true)
    @Mapping(target = "detalleSolicitud", ignore = true)
    void updateEntityFromDto(AtencionSolicitudUpdateDto dto, @MappingTarget AtencionSolicitud entity);

    @Mapping(source = "atencionServicios", target = "idsServicios", qualifiedByName = "atencionServiciosToIds")
    @Mapping(source = "prioridad.idPrioridad", target = "idPrioridad")
    @Mapping(source = "detalleSolicitud.idDetalleSolicitud", target = "idDetalleSolicitud")
    AtencionSolicitudResponseDto toResponseDto(AtencionSolicitud entity);

    List<AtencionSolicitudResponseDto> toResponseDtoList(List<AtencionSolicitud> entities);

    @Named("atencionServiciosToIds")
    default List<Long> atencionServiciosToIds(List<AtencionServicio> atencionServicios) {
        if (atencionServicios == null) return List.of();
        return atencionServicios.stream()
                .filter(as -> as.getServicio() != null)
                .map(as -> as.getServicio().getIdServicio())
                .collect(Collectors.toList());
    }
}

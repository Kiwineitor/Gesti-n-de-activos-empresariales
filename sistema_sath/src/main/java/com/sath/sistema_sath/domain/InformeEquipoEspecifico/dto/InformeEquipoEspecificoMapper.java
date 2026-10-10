package com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto;

import com.sath.sistema_sath.domain.InformeEquipoEspecifico.InformeEquipoEspecifico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InformeEquipoEspecificoMapper {

    @Mapping(target = "detalleSolicitud", ignore = true)
    @Mapping(target = "idInformeEquipoEspecifico", ignore = true)
    InformeEquipoEspecifico toEntity(InformeEquipoEspecificoCreateDto dto);

    @Mapping(source = "detalleSolicitud.idDetalleSolicitud", target = "idDetalleSolicitud")
    InformeEquipoEspecificoResponseDto toResponseDto(InformeEquipoEspecifico Entity);

    List<InformeEquipoEspecificoResponseDto> toResponseDtoList(List<InformeEquipoEspecifico> entities);

}

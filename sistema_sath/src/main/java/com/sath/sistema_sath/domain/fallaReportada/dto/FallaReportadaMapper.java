package com.sath.sistema_sath.domain.fallaReportada.dto;

import com.sath.sistema_sath.domain.fallaReportada.FallaReportada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FallaReportadaMapper {

    @Mapping(target = "idFallaReportada", ignore = true)
    @Mapping(target = "falla", ignore = true)
    @Mapping(target = "detalleSolicitud", ignore = true)
    FallaReportada toEntity(FallaReportadaCreateDto dto);

    @Mapping(source = "falla.nombreFall", target = "nombreFalla")
    @Mapping(source = "detalleSolicitud.idDetalleSolicitud", target = "idDetalleSolicitud")
    FallaReportadaResponseDto toResponseDto(FallaReportada entity);

    @Mapping(target = "falla", ignore = true)
    @Mapping(target = "detalleSolicitud", ignore = true)
    void updateEntityFromDto(FallaReportadaUpdateDto dto, @MappingTarget FallaReportada entity);

    List<FallaReportadaResponseDto> toResponseDtoList(List<FallaReportada> entities);
}

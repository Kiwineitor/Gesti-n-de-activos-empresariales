package com.sath.sistema_sath.domain.cargo.dto;

import com.sath.sistema_sath.domain.cargo.Cargo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoMapper {

    Cargo toEntity(CargoCreateDto dto);

    void updateEntityFromDto (CargoUpdateDto Dto, @MappingTarget Cargo entity);

    CargoResponseDto toResponseDto(Cargo entity);

    List<CargoResponseDto> toResponseDtoList(List<Cargo> entities);
}

package com.sath.sistema_sath.domain.cargo;

import com.sath.sistema_sath.domain.cargo.dto.CargoCreateDto;
import com.sath.sistema_sath.domain.cargo.dto.CargoResponseDto;
import com.sath.sistema_sath.domain.cargo.interfaz.CargoProjection;

import java.util.List;
import java.util.Map;

public interface CargoService {

    CargoResponseDto create(CargoCreateDto Dto);

    List<CargoResponseDto> findAll();

    CargoResponseDto findById(Long id);

    void delete(Long id);

    List<Map<String, Object>> listaParaElFrontend();


    List<CargoProjection> findByEstado(String estado);

}

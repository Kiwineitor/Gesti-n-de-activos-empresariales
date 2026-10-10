package com.sath.sistema_sath.domain.servicio;

import com.sath.sistema_sath.domain.servicio.dto.ServicioCreateDto;
import com.sath.sistema_sath.domain.servicio.dto.ServicioResponseDto;

import java.util.List;
import java.util.Map;

public interface ServicioService {
 
    ServicioResponseDto create(ServicioCreateDto dto);
 
    // Solo ACTIVOS — para la tabla de la vista
//    List<ServicioResponseDto> listarActivos();
 
    ServicioResponseDto findById(Long id);

    List<Map<String, Object>> listaServicio();

 
    // Edita nombreServ — estado no se toca
    ServicioResponseDto update(Long id, ServicioCreateDto dto);
 
    void delete(Long id);
}
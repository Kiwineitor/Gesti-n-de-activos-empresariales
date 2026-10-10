package com.sath.sistema_sath.domain.prioridad;

import com.sath.sistema_sath.domain.prioridad.dto.PrioridadCreateDto;
import com.sath.sistema_sath.domain.prioridad.dto.PrioridadEstadoDto;
import com.sath.sistema_sath.domain.prioridad.dto.PrioridadUpdateDto;
import com.sath.sistema_sath.domain.prioridad.dto.PrioridadresponseDto;

import java.util.List;
import java.util.Map;

public interface PrioridadService {

  PrioridadresponseDto create (PrioridadCreateDto dto);

  List<PrioridadresponseDto> findAll();

  PrioridadresponseDto findById(Long id);

  PrioridadresponseDto update(Long id, PrioridadUpdateDto dto);

  void delete(Long id);

  List<Map<String, Object>> listaPrioridad();

  public void cambiarEstado (long id, PrioridadEstadoDto dto);
}

package com.sath.sistema_sath.domain.atencionSolicitud;

import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtencionSolicitudCreateDto;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtencionSolicitudResponseDto;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtencionSolicitudUpdateDto;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtenderDetalleDto;

import java.util.List;
import java.util.Map;


public interface AtencionSolicitudService {

    AtencionSolicitudResponseDto create(AtencionSolicitudCreateDto dto);

    List<AtencionSolicitudResponseDto> findAll();

    AtencionSolicitudResponseDto findById(Long id);

    AtencionSolicitudResponseDto update(Long id, AtencionSolicitudUpdateDto dto);

    void delete(Long id);

    List<Map<String,Object>> listaParaElFrontend();

    AtencionSolicitudResponseDto atender(AtenderDetalleDto dto, String username);

    List<Map<String, Object>> listaAtenciones();

    List<Map<String, Object>> listaAtencionesPorTecnico(String username);

    List<Map<String, Object>> listaParaAsignar();
}
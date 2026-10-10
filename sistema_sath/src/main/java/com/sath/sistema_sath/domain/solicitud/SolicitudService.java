package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.dto.CambiarEstadoSolicitudDto;

import com.sath.sistema_sath.domain.solicitud.dto.SolicitudCreateDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudDetalleResponseDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudResponseDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudUpdateDto;

import java.util.List;
import java.util.Map;

public interface SolicitudService {

    SolicitudResponseDto create (SolicitudCreateDto dto);

    List<SolicitudResponseDto> findAll();

    List<SolicitudResponseDto> findAllByEstado(EstadoSolicitudEnum estado);

    SolicitudResponseDto findById(Long id);

    SolicitudResponseDto update(Long id, SolicitudUpdateDto dto);

    void delete(Long id);

    List<Map<String, Object>> listaSolicitud();

    public void cambiarEstado(Long id, CambiarEstadoSolicitudDto dto);

    SolicitudDetalleResponseDto getDetalle(Long id);

    List<Map<String, Object>> listaSolicitudFinalizado();

    List<Map<String, Object>> listaSolicitudPorTecnico(String username);

    Long colaMax();

    List<Map<String, Object>> listaSolicitudServicioTecnico();

}

package com.sath.sistema_sath.domain.detalleSolicitud;

import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudCreateDto;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudResponseDto;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudUpdateDto;

import java.util.List;
import java.util.Map;


public interface DetalleSolicitudService {

    DetalleSolicitudResponseDto create (DetalleSolicitudCreateDto dto);

    List<DetalleSolicitudResponseDto> findAll();

    DetalleSolicitudResponseDto findById(Long id);

    DetalleSolicitudResponseDto update(Long id, DetalleSolicitudUpdateDto dto);

    void delete(Long id);

    public void tipoInforme(Long id, TipoInformeEnum dto);

    List<Map<String, Object>> listaParaELFrontend();
}

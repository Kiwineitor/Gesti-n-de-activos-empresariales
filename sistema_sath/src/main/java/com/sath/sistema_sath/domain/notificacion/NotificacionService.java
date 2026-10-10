package com.sath.sistema_sath.domain.notificacion;

import com.sath.sistema_sath.domain.notificacion.dto.NotificacionDto;

import java.util.List;

public interface NotificacionService {
    void guardar(String tipo, String titulo, String descripcion);
    List<NotificacionDto> listar();
    void marcarLeida(Long id);
    void marcarTodasLeidas();
}
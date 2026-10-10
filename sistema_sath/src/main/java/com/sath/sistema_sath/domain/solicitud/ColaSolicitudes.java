package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.domain.solicitud.dto.SolicitudResponseDto;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ColaSolicitudes {

    private final Map<Long, SolicitudResponseDto> cola = new LinkedHashMap<>();

    public void agregar(SolicitudResponseDto solicitud) {
        cola.put(solicitud.idSolicitud(), solicitud);
    }

    public Collection<SolicitudResponseDto> obtenerTodas() {
        return cola.values();
    }

    public void remover(Long idSolicitud) {
        cola.remove(idSolicitud);
    }

    public int getPosicion(Long idSolicitud) {
        int pos = 1;
        for (Long key : cola.keySet()) {
            if (key.equals(idSolicitud)) return pos;
            pos++;
        }
        return -1;
    }

    public int size() {
        return cola.size();
    }
}

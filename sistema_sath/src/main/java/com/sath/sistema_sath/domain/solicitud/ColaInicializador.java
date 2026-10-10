package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;
import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudMapper;
import com.sath.sistema_sath.tools.colas.ColaPriorizadaSolicitudes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ColaInicializador implements ApplicationRunner {

    private final SolicitudRepository repo;
    private final ColaSolicitudes colaSolicitudes;
    private final ColaPriorizadaSolicitudes colaPriorizada;
    private final SolicitudMapper mapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Solicitud> pendientes = repo.findByEstadoSolOrderByIdSolicitudAsc(EstadoSolicitudEnum.PENDIENTE);
        pendientes.forEach(s -> colaSolicitudes.agregar(mapper.toResponseDto(s)));
//        log.info("Cola PENDIENTE inicializada con {} solicitudes", pendientes.size());

        List<Solicitud> enMant = repo.findByEstadoSolOrderByIdSolicitudAsc(EstadoSolicitudEnum.EN_MANTENIMIENTO);
        int cargadas = 0;
        for (Solicitud s : enMant) {
            PrioridadEnum prioridad = obtenerPrioridad(s);
            if (prioridad != null) {
                colaPriorizada.agregarOActualizar(s.getIdSolicitud(), prioridad, mapper.toResponseDto(s));
                cargadas++;
            }
        }
//        log.info("Cola EN_MANTENIMIENTO inicializada con {} solicitudes", cargadas);
    }

    private PrioridadEnum obtenerPrioridad(Solicitud s) {
        if (s.getDetalleSolicitud() == null) return null;
        return s.getDetalleSolicitud().stream()
                .filter(d -> d.getAtencionSolicitudes() != null)
                .flatMap(d -> d.getAtencionSolicitudes().stream())
                .filter(a -> a.getPrioridad() != null && a.getPrioridad().getNivel() != null)
                .map(a -> a.getPrioridad().getNivel())
                .findFirst()
                .orElse(null);
    }
}

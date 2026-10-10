package com.sath.sistema_sath.domain.solicitud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModoAutoAsignarService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicBoolean activo = new AtomicBoolean(false);

    public boolean isActivo() {
        return activo.get();
    }

    public boolean toggle() {
        boolean prev, next;
        do {
            prev = activo.get();
            next = !prev;
        } while (!activo.compareAndSet(prev, next));
        log.info("Modo auto-asignación {}", next ? "ACTIVADO" : "DESACTIVADO");
        messagingTemplate.convertAndSend("/topic/solicitudes/modo-auto-asignar",
                Map.of("activo", next));
        return next;
    }
}

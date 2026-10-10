package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.domain.notificacion.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitudNotificacionListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificacionService notificacionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEstadoCambiado(EstadoCambiadoEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/solicitudes/estado", event.dto());
            notificacionService.guardar("estado", "Cambio de estado",
                    "Solicitud #" + event.dto().nroSol() + " → " + event.nuevoEstado().name());
        } catch (Exception e) {
            log.warn("Error notificando cambio de estado para solicitud {}: {}",
                    event.dto().idSolicitud(), e.getMessage());
        }
    }
}

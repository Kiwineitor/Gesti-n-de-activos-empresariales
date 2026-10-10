package com.sath.sistema_sath.SolicitudLondra;

import com.sath.sistema_sath.SolicitudLondra.dto.SolicitudLondraDTO;
import com.sath.sistema_sath.domain.solicitud.ColaSolicitudes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mantenimiento")
@Slf4j
@RequiredArgsConstructor
public class MantenimientoWebhookController {

    private final MantenimientoService mantenimientoService;
    private final LondraPollingService londraPollingService;
    private final ColaSolicitudes colaSolicitudes;

    @Value("${londra.saul.api-key}")
    private String saulApiKey;

    @Value("${londra.saul.password}")
    private String saulPassword;

    @PostMapping("/recibir-solicitud")
    public ResponseEntity<String> recibirSolicitud(@RequestBody List<SolicitudLondraDTO> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            return ResponseEntity.badRequest().body("La lista de solicitudes está vacía");
        }

        int procesadas = 0;
        for (SolicitudLondraDTO solicitud : solicitudes) {
            if (solicitud.getFuncionario() == null || solicitud.getFuncionario().getCi() == null) {
                log.warn("Solicitud ignorada: CI del funcionario ausente");
                continue;
            }
            if (solicitud.getIdSolicitudLondra() == null) {
                log.warn("Solicitud ignorada: ID de solicitud ausente");
                continue;
            }
            try {
                mantenimientoService.procesarSolicitudLondra(solicitud);
                procesadas++;
            } catch (Exception e) {
                log.error("Error procesando solicitud id={}: {}", solicitud.getIdSolicitudLondra(), e.getMessage(), e);
            }
        }

        return ResponseEntity.ok("Procesadas " + procesadas + " de " + solicitudes.size() + " solicitudes");
    }


    @PostMapping("/recibir-notificacion-londra")
    public ResponseEntity<Map<String, Object>> recibirNotificacionLondra(@RequestBody Map<String, String> body) {
        String evento = body.get("evento");
        String apiKey = body.get("APIKey");
        String contraseña = body.get("contraseña");

        if (!saulApiKey.equals(apiKey) || !saulPassword.equals(contraseña)) {
            log.warn("ALERTA: Credenciales inválidas desde Saul");
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }

        if (!"Sincronizacion_Activos".equals(evento)) {
            return ResponseEntity.status(400).body(Map.of("error", "Evento desconocido"));
        }

        londraPollingService.consultarYObtenerPosiciones();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ColaMax", colaSolicitudes.size());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping(value = "/recibir-firma-londra", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> test(@RequestBody(required = false) String body) {
        log.info("Recibido mensaje de firma de Londra");

        // Imprimir el cuerpo en la consola
        if (body != null) {
            System.out.println("--- INICIO DEL CUERPO RECIBIDO ---");
            System.out.println(body);
            System.out.println("--- FIN DEL CUERPO RECIBIDO ---");
        } else {
            System.out.println("El cuerpo de la petición vino vacío.");
        }

        return ResponseEntity.ok("OK");
    }
}

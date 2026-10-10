package com.sath.sistema_sath.SolicitudLondra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LondraNotificacionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${londra.api.url}")
    private String londraUrl;

    @Value("${londra.api.key}")
    private String apiKey;



    // ✅ Solo este método hace todo
    public void notificarActualizacionCola(List<Map<String, Object>> payloads) {
        try {
            log.debug("📤 Cola máxima enviada a Londra ({} items):\n{}",
                    payloads.size(),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payloads));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(payloads, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    londraUrl, HttpMethod.POST, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Cola notificada a Londra | {} items | respuesta: {}",
                        payloads.size(), response.getBody());
            } else {
                log.warn("⚠️ Respuesta no exitosa | Status: {} | Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Error notificando cola a Londra | error: {}", e.getMessage(), e);
        }
    }
    public void notificarPosicionCola(Integer mantIdLondra, int posicion) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("mant_id", mantIdLondra);
            body.put("Estado", "PENDIENTE");
            body.put("Cola", posicion);

//            log.info("📤 JSON enviado a Londra:\n{}",
//                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(londraUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Posición en cola notificada a Londra | mant_id: {} | cola: {}", mantIdLondra, posicion);
            } else {
                log.warn("⚠️ Respuesta no exitosa de Londra (cola) | Status: {} | Body: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Error notificando posición de cola a Londra | mant_id: {} | error: {}", mantIdLondra, e.getMessage(), e);
        }
    }

    public void notificarCambioEstado(Integer mantIdLondra, String estado, String Observacion) {

        try {
            Map<String, Object> body = Map.of(
                    "mant_id", mantIdLondra,
                    "Estado", estado,
                    "Observacion", Observacion != null ? Observacion : "Sin observación"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    londraUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Notificación exitosa a Londra | mant_id: {} | estado: {} | respuesta: {}",
                        mantIdLondra, estado, response.getBody());
            } else {
                log.warn("⚠️ Respuesta no exitosa de Londra | Status: {} | Body: {}",
                        response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("❌ Error notificando a Londra | mant_id: {} | estado: {} | error: {}",
                    mantIdLondra, estado, e.getMessage(), e);
        }
    }

    /**
     * Notifica a Londra cuando un técnico cataloga una falla y envía la posición actual en cola.
     *
     * @param mantIdLondra  ID de la solicitud en Londra (mant_id)
     * @param nombreFalla   Nombre de la falla catalogada por el técnico
     * @param posicionCola  Posición global en la cola priorizada (-1 si no está en cola aún)
     */
    public void notificarCatalogacion(Integer mantIdLondra, String nombreFalla, int posicionCola) {
        try {
            String Observacion = posicionCola > 0
                    ? "Falla catalogada: \"" + nombreFalla + "\". Su solicitud está en la posición " + posicionCola + " de la cola."
                    : "Falla catalogada: \"" + nombreFalla + "\". Su solicitud aún no tiene prioridad asignada.";

            Map<String, Object> body = new HashMap<>();
            body.put("mant_id", mantIdLondra);
            body.put("Estado", "EN_REVISION");
            body.put("FallaCatalogada", nombreFalla);
            body.put("Observacion", Observacion);
            if (posicionCola > 0) {
                body.put("PosicionCola", posicionCola);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    londraUrl, HttpMethod.POST, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Catalogación notificada a Londra | mant_id: {} | falla: '{}' | posición: {}",
                        mantIdLondra, nombreFalla, posicionCola > 0 ? posicionCola : "sin asignar");
            } else {
                log.warn("⚠️ Respuesta no exitosa de Londra (catalogación) | Status: {} | Body: {}",
                        response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("❌ Error notificando catalogación a Londra | mant_id: {} | falla: '{}' | error: {}",
                    mantIdLondra, nombreFalla, e.getMessage(), e);
        }
    }
}

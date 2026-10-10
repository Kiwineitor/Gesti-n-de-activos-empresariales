package com.sath.sistema_sath.SolicitudLondra;

import com.sath.sistema_sath.SolicitudLondra.dto.SolicitudLondraDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LondraPollingService {

    private final MantenimientoService mantenimientoService;

    @Value("${londra.polling.url}")
    private String pollingUrl;

    @Value("${londra.polling.usuario}")
    private String usuario;

    @Value("${londra.polling.contrasena}")
    private String contrasena;

    public List<Integer> consultarYObtenerPosiciones() {
        log.info("Polling Londra (webhook): consultando solicitudes pendientes...");
        List<Integer> posiciones = new ArrayList<>();
        try {
            RestClient client = RestClient.builder()
                    .baseUrl(pollingUrl)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeaders(h -> h.setBasicAuth(usuario, contrasena))
                    .build();

            List<SolicitudLondraDTO> solicitudes = client.get()
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (solicitudes == null || solicitudes.isEmpty()) {
                log.info("Polling Londra: sin solicitudes nuevas");
                return posiciones;
            }

            for (SolicitudLondraDTO dto : solicitudes) {
                if (dto.getFuncionario() == null || dto.getFuncionario().getCi() == null) continue;
                if (dto.getIdSolicitudLondra() == null) continue;
                try {
                    int pos = mantenimientoService.procesarSolicitudLondra(dto);
                    if (pos > 0) posiciones.add(pos);
                } catch (Exception e) {
                    log.error("Error procesando solicitud id={}: {}", dto.getIdSolicitudLondra(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Polling Londra: error al consultar {}: {}", pollingUrl, e.getMessage());
        }
        return posiciones;
    }

    @Async
    public void consultarSolicitudesPendientes() {
        log.info("Polling Londra: consultando solicitudes pendientes...");
        try {
            RestClient client = RestClient.builder()
                    .baseUrl(pollingUrl)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeaders(h -> h.setBasicAuth(usuario, contrasena))
                    .build();

            List<SolicitudLondraDTO> solicitudes = client.get()
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (solicitudes == null || solicitudes.isEmpty()) {
                log.info("Polling Londra: sin solicitudes nuevas");
                return;
            }

            log.info("Polling Londra: {} solicitud(es) recibida(s)", solicitudes.size());
            int procesadas = 0;
            for (SolicitudLondraDTO dto : solicitudes) {
                if (dto.getFuncionario() == null || dto.getFuncionario().getCi() == null) {
                    log.warn("Solicitud ignorada: CI del funcionario ausente (id={})", dto.getIdSolicitudLondra());
                    continue;
                }
                if (dto.getIdSolicitudLondra() == null) {
                    log.warn("Solicitud ignorada: ID de solicitud ausente");
                    continue;
                }
                try {
                    mantenimientoService.procesarSolicitudLondra(dto);
                    procesadas++;
                } catch (Exception e) {
                    log.error("Error procesando solicitud id={}: {}", dto.getIdSolicitudLondra(), e.getMessage(), e);
                }
            }
            log.info("Polling Londra: {}/{} solicitudes procesadas correctamente", procesadas, solicitudes.size());

        } catch (Exception e) {
            log.error("Polling Londra: error al consultar {}: {}", pollingUrl, e.getMessage());
        }
    }
}

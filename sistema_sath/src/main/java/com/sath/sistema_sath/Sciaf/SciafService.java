package com.sath.sistema_sath.Sciaf;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SciafService {
    private final RepoGenerico repoGenerico;
    private final RestTemplate restTemplate;

    private static final String SCIAF_FICHA_URL =
            "https://sciaf.uap.edu.bo/api/activos/{codigo}/ficha";

    public List<Map<String, Object>> obtenerHistorialActivo(String codigoActivo) {
        return repoGenerico.paraSciafUwU(codigoActivo);
    }

    public Map<String, Object> obtenerFichaActivo(String codigo) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    SCIAF_FICHA_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    codigo
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}

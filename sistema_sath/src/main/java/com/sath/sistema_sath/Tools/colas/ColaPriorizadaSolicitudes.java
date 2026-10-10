package com.sath.sistema_sath.tools.colas;


import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ColaPriorizadaSolicitudes {

    // 3 colas internas, una por nivel de prioridad
    private final Map<PrioridadEnum, Queue<SolicitudConPrioridad>> colasPorPrioridad = new EnumMap<>(PrioridadEnum.class);

    // Índice rápido: idSolicitud → su objeto con prioridad (para reposicionar en O(1))
    private final Map<Long, SolicitudConPrioridad> indiceSolicitudes = new ConcurrentHashMap<>();

    // Contador global para ordenar dentro de la misma prioridad (FIFO)
    private long contadorSecuencial = 0;

    public ColaPriorizadaSolicitudes() {
        // Inicializar las 3 colas
        for (PrioridadEnum prioridad : PrioridadEnum.values()) {
            colasPorPrioridad.put(prioridad, new LinkedList<>());
        }
    }

    /**
     * Agrega o actualiza una solicitud en la cola priorizada
     */
    public synchronized void agregarOActualizar(Long idSolicitud, PrioridadEnum nuevaPrioridad, SolicitudResponseDto datos) {

        // Si ya existe, removerla primero (para reposicionar)
        if (indiceSolicitudes.containsKey(idSolicitud)) {
            removerInterna(idSolicitud);
        }

        // Crear nuevo registro con orden de llegada dentro de su prioridad
        SolicitudConPrioridad entrada = new SolicitudConPrioridad(
                idSolicitud,
                nuevaPrioridad,
                contadorSecuencial++,
                datos
        );

        // Agregar a la cola correspondiente
        colasPorPrioridad.get(nuevaPrioridad).offer(entrada);
        indiceSolicitudes.put(idSolicitud, entrada);

//        log.info("Solicitud {} agregada/actualizada en prioridad {} | Posición global: {}",
//                idSolicitud, nuevaPrioridad, calcularPosicionGlobal(idSolicitud));
    }

    /**
     * Calcula la posición GLOBAL de una solicitud (considerando todas las prioridades superiores)
     */
    public synchronized int calcularPosicionGlobal(Long idSolicitud) {
        SolicitudConPrioridad entrada = indiceSolicitudes.get(idSolicitud);
        if (entrada == null) return -1; // No está en cola

        int posicion = 1; // Las posiciones son 1-based

        // 1. Contar todas las solicitudes de prioridades SUPERIORES
        for (PrioridadEnum prioridadSuperior : getPrioridadesSuperiores(entrada.prioridad())) {
            posicion += colasPorPrioridad.get(prioridadSuperior).size();
        }

        // 2. Contar las de SU misma prioridad que llegaron antes (por ordenSecuencial)
        Queue<SolicitudConPrioridad> colaMismaPrioridad = colasPorPrioridad.get(entrada.prioridad());
        for (SolicitudConPrioridad otra : colaMismaPrioridad) {
            if (otra.ordenSecuencial() < entrada.ordenSecuencial()) {
                posicion++;
            }
        }

        return posicion;
    }

    /**
     * Remueve una solicitud de la cola (ej: cuando se finaliza)
     */
    public synchronized void remover(Long idSolicitud) {
        if (indiceSolicitudes.containsKey(idSolicitud)) {
            removerInterna(idSolicitud);
            log.debug("Solicitud {} removida de cola priorizada", idSolicitud);
        }
    }

    /**
     * Obtiene todas las solicitudes en orden de atención (ALTO → MEDIO → BAJO, FIFO dentro de cada una)
     */
    public synchronized List<SolicitudResponseDto> obtenerEnOrdenDeAtencion() {
        List<SolicitudResponseDto> resultado = new ArrayList<>();

        // Orden de procesamiento: ALTO primero, luego MEDIO, luego BAJO
        List<PrioridadEnum> ordenProcesamiento = Arrays.asList(
                PrioridadEnum.ALTO,
                PrioridadEnum.MEDIO,
                PrioridadEnum.BAJO
        );

        for (PrioridadEnum prioridad : ordenProcesamiento) {
            Queue<SolicitudConPrioridad> cola = colasPorPrioridad.get(prioridad);
            // Crear copia ordenada por ordenSecuencial (FIFO)
            List<SolicitudConPrioridad> ordenadas = new ArrayList<>(cola);
            ordenadas.sort(Comparator.comparingLong(SolicitudConPrioridad::ordenSecuencial));

            for (SolicitudConPrioridad entrada : ordenadas) {
                resultado.add(entrada.datos());
            }
        }

        return resultado;
    }

    /**
     * Helper: remueve internamente sin logs
     */
    private void removerInterna(Long idSolicitud) {
        SolicitudConPrioridad entrada = indiceSolicitudes.remove(idSolicitud);
        if (entrada != null) {
            colasPorPrioridad.get(entrada.prioridad()).remove(entrada);
        }
    }

    /**
     * Helper: devuelve las prioridades que tienen precedencia sobre la dada
     */
    private List<PrioridadEnum> getPrioridadesSuperiores(PrioridadEnum actual) {
        return switch (actual) {
            case ALTO -> List.of(); // Nada es más prioritario que ALTO
            case MEDIO -> List.of(PrioridadEnum.ALTO);
            case BAJO -> List.of(PrioridadEnum.ALTO, PrioridadEnum.MEDIO);
        };
    }

    /**
     * Record interno para mantener metadata de ordenamiento
     */
    private record SolicitudConPrioridad(
            Long idSolicitud,
            PrioridadEnum prioridad,
            long ordenSecuencial,  // Para FIFO dentro de la misma prioridad
            SolicitudResponseDto datos
    ) {}
}
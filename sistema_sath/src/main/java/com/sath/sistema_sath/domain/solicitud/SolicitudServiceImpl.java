package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.SolicitudLondra.LondraNotificacionService;
import com.sath.sistema_sath.domain.notificacion.NotificacionService;
import com.sath.sistema_sath.tools.colas.ColaPriorizadaSolicitudes;
import com.sath.sistema_sath.domain.InformeEquipoEspecifico.InformeEquipoEspecificoRepository;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.InformeEquipoGeneralRepository;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionSolicitudRepository;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitudRepository;
import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;
import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.dto.CambiarEstadoSolicitudDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudCreateDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudDetalleResponseDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudMapper;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudResponseDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudUpdateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository repo;
    private final DetalleSolicitudRepository detalleRepo;
    private final AtencionSolicitudRepository atencionRepo;
    private final InformeEquipoEspecificoRepository informeEspRepo;
    private final InformeEquipoGeneralRepository informeGenRepo;
    private final SolicitudMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ColaSolicitudes colaSolicitudes;
    private final LondraNotificacionService londraNotificacionService;
    private final ColaPriorizadaSolicitudes colaPriorizada;
    private final NotificacionService notificacionService;
    private final ApplicationEventPublisher publisher;

    @Override
    public SolicitudResponseDto create(SolicitudCreateDto dto) {
        Solicitud entity = mapper.toEntity(dto);
        SolicitudResponseDto response = mapper.toResponseDto(repo.save(entity));

        colaSolicitudes.agregar(response);
        messagingTemplate.convertAndSend("/topic/solicitudes/nueva", response);

        String desc = "Solicitud #" + response.nroSol() + (response.descripcionSol() != null ? " — " + response.descripcionSol() : "");
        notificacionService.guardar("nueva", "Nueva solicitud", desc);

        return response;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<SolicitudResponseDto> findAll() {
        return buildWithQueuePositions(repo.findAll());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<SolicitudResponseDto> findAllByEstado(EstadoSolicitudEnum estado) {
        return buildWithQueuePositions(repo.findByEstadoSolOrderByIdSolicitudAsc(estado));
    }

    private List<SolicitudResponseDto> buildWithQueuePositions(List<Solicitud> solicitudes) {
        List<Long> colaIdsPend = colaSolicitudes.obtenerTodas().stream()
                .map(SolicitudResponseDto::idSolicitud)
                .toList();

        List<SolicitudResponseDto> sorted = mapper.toResponseDtoList(solicitudes).stream()
                .sorted(Comparator
                        .comparingInt((SolicitudResponseDto s) -> pesoEstado(s.estadoSol()))
                        .thenComparingInt(s -> claveOrdenEnCola(s.idSolicitud(), colaIdsPend))
                        .thenComparingLong(s -> s.idSolicitud() != null ? s.idSolicitud() : Long.MAX_VALUE))
                .toList();

        Map<String, Integer> contadores = new HashMap<>();
        return sorted.stream().map(s -> {
            int pos = 0;
            if ("PENDIENTE".equals(s.estadoSol()) || "EN_MANTENIMIENTO".equals(s.estadoSol())) {
                pos = contadores.merge(s.estadoSol(), 1, Integer::sum);
            }
            return new SolicitudResponseDto(
                    s.idSolicitud(), s.estadoSol(), s.nroSol(), s.gestionSol(),
                    s.fechaSol(), s.descripcionSol(), s.observacionFicha(), s.mantIdLondra(), s.nroSolExterna(),
                    pos
            );
        }).toList();
    }

    private int pesoEstado(String estado) {
        if (estado == null) return 999;
        return switch (estado) {
            case "PENDIENTE" -> 0;
            case "EN_MANTENIMIENTO" -> 1;
            default -> 2;
        };
    }

    private int claveOrdenEnCola(Long id, List<Long> colaIdsPend) {
        if (id == null) return Integer.MAX_VALUE;
        int idx = colaIdsPend.indexOf(id);
        if (idx >= 0) return idx;
        int pos = colaPriorizada.calcularPosicionGlobal(id);
        return pos > 0 ? 100_000 + pos : Integer.MAX_VALUE;
    }

    @Override
    public SolicitudResponseDto findById(Long id) {
        Solicitud entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrado con ID: " + id));
        return mapper.toResponseDto(entity);
    }

    @Override
    public SolicitudResponseDto update(Long id, SolicitudUpdateDto dto) {
        Solicitud entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrado con ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public void delete(Long id) {
        Solicitud entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }

    @Override
    public List<Map<String, Object>> listaSolicitud() {
        return repo.listaSolicitud();
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, CambiarEstadoSolicitudDto dto) {

        if (id == null || dto == null || dto.estado() == null) {
            log.error("Parámetros inválidos: id={}, dto={}", id, dto);
            throw new IllegalArgumentException("ID y estado son requeridos");
        }

        Solicitud solicitud = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        EstadoSolicitudEnum estadoActual = solicitud.getEstadoSol();
        EstadoSolicitudEnum nuevoEstado = dto.estado();

        if (estadoActual == nuevoEstado) {
            log.info("Solicitud {} ya está en estado {}", id, nuevoEstado);
            return;
        }

        try {
//            log.info("📋 Estado cambiado:\n{}",
//                    objectMapper.writerWithDefaultPrettyPrinter()
//                            .writeValueAsString(Map.of(
//                                    "idSolicitud", id,
//                                    "estadoAnterior", estadoActual.name(),
//                                    "nuevoEstado", nuevoEstado.name(),
//                                    "mantIdLondra", solicitud.getMantIdLondra() != null ? solicitud.getMantIdLondra() : "null"
//                            )));
        } catch (Exception e) {
            log.error("Error serializando log: {}", e.getMessage());
        }

        solicitud.setEstadoSol(nuevoEstado);
        Solicitud guardada = repo.save(solicitud);

        // Sincronizar estado de los activos (DetalleSolicitud) asociados
        String estadoDetalle = estadoDetalleParaSolicitud(nuevoEstado);
        detalleRepo.actualizarEstadoPorSolicitud(id, estadoDetalle);
        log.info("Activos de solicitud {} actualizados a estado '{}'", id, estadoDetalle);

        SolicitudResponseDto responseDto = mapper.toResponseDto(guardada);

        if (nuevoEstado != EstadoSolicitudEnum.FINALIZADO) {
            String nivelStr = atencionRepo.findPrioridadNivelBySolicitudId(id);
            if (nivelStr != null) {
                colaPriorizada.agregarOActualizar(id, PrioridadEnum.valueOf(nivelStr), responseDto);
            }
        }

        log.info("Solicitud {} actualizada: {} → {}", id, estadoActual, nuevoEstado);

        publisher.publishEvent(new EstadoCambiadoEvent(responseDto, nuevoEstado));

        // Al pasar a EN_MANTENIMIENTO: armar array completo y mandar un solo POST a Londra
        if (nuevoEstado == EstadoSolicitudEnum.EN_MANTENIMIENTO) {
            colaSolicitudes.remover(id); // primero removés al que sale

            List<Map<String, Object>> payloads = new ArrayList<>();

            // 1️⃣ El que salió → EN_MANTENIMIENTO, Cola: 0
            if (solicitud.getMantIdLondra() != null) {
                Map<String, Object> salio = new LinkedHashMap<>();
                salio.put("mant_id", solicitud.getMantIdLondra());
                salio.put("Estado", "EN_MANTENIMIENTO");
                salio.put("Cola", 0);
                salio.put("Observacion", dto.observacion() != null ? dto.observacion() : "");
                payloads.add(salio);
            }

            // 2️⃣ Los que quedan → PENDIENTE con su nueva posición (desde BD, no desde cola en memoria)
            int pos = 1;
            List<SolicitudLondraView> pendientes = repo.findLondraViewByEstadoSolOrderByIdSolicitudAsc(
                    EstadoSolicitudEnum.PENDIENTE);
            for (SolicitudLondraView s : pendientes) {
                if (s.getMantIdLondra() == null) { pos++; continue; }
                Map<String, Object> pendiente = new LinkedHashMap<>();
                pendiente.put("mant_id", s.getMantIdLondra());
                pendiente.put("Estado", "PENDIENTE");
                pendiente.put("Cola", pos++);
                payloads.add(pendiente);
            }

            if (!payloads.isEmpty()) {
                londraNotificacionService.notificarActualizacionCola(payloads);
            }

            log.debug("Solicitud {} removida de cola PENDIENTE → EN_MANTENIMIENTO", id);
        }

        // Si el estado es FINALIZADO remover de ambas colas y cerrar fecha fin de reparación
        if (nuevoEstado == EstadoSolicitudEnum.FINALIZADO) {
            colaSolicitudes.remover(id);
            colaPriorizada.remover(id);
            atencionRepo.cerrarFechaFinPorSolicitud(id);
            log.debug("Solicitud {} removida de ambas colas — FINALIZADO", id);

            if (solicitud.getMantIdLondra() != null) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("mant_id", solicitud.getMantIdLondra());
                payload.put("Estado", "FINALIZADO");
                payload.put("Cola", 0);
                payload.put("Observacion", dto.observacion() != null ? dto.observacion() : "");
                londraNotificacionService.notificarActualizacionCola(List.of(payload));
            }
        }
    }

    @Override
    @Transactional
    public SolicitudDetalleResponseDto getDetalle(Long id) {
        Solicitud sol = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));

        String solicitante = "";
        String celular = "";
        String correo = "";
        String unidad = "";
        String cargo = "";
        if (sol.getAdministrativo() != null) {
            var adm = sol.getAdministrativo();
            if (adm.getPersona() != null) {
                var p = adm.getPersona();
                solicitante = (p.getNombrePer() + " " + p.getApPaternoPer() + " " + p.getApMaternoPer()).trim();
                celular = p.getNroCellPer() != null ? p.getNroCellPer() : "";
                correo = p.getCorreoPer() != null ? p.getCorreoPer() : "";
            }
            if (adm.getUnidad() != null) unidad = adm.getUnidad().getNombreUni();
            if (adm.getCargo() != null) cargo = adm.getCargo().getNombreCar();
        }

        List<SolicitudDetalleResponseDto.DetalleActivoDto> activos = sol.getDetalleSolicitud() == null
                ? List.of()
                : sol.getDetalleSolicitud().stream().map(det -> {
            String codigoActivo = det.getActivo() != null ? det.getActivo().getCodigoAct() : "";
            String descripcionActivo = det.getActivo() != null ? det.getActivo().getDescripcionAct() : "";
            String tipoActivo = (det.getActivo() != null && det.getActivo().getTipoActivo() != null)
                    ? det.getActivo().getTipoActivo().getNombreTipoAct() : "";
            Long idTipoActivo = (det.getActivo() != null && det.getActivo().getTipoActivo() != null)
                    ? det.getActivo().getTipoActivo().getIdTipoActivo() : null;

            Long idFallaReportada = (det.getFallasReportadas() != null && !det.getFallasReportadas().isEmpty())
                    ? det.getFallasReportadas().get(0).getIdFallaReportada() : null;
            String problemaEspecifico = (det.getFallasReportadas() != null && !det.getFallasReportadas().isEmpty())
                    ? det.getFallasReportadas().get(0).getDescripcionFallRep() : "";

            String prioridad = (det.getAtencionSolicitudes() != null && !det.getAtencionSolicitudes().isEmpty()
                    && det.getAtencionSolicitudes().get(0).getPrioridad() != null)
                    ? det.getAtencionSolicitudes().get(0).getPrioridad().getNivel().name() : null;

            boolean tieneAtencion = det.getAtencionSolicitudes() != null && !det.getAtencionSolicitudes().isEmpty();

            var informeEsp = informeEspRepo.findByDetalleSolicitudIdDetalleSolicitud(det.getIdDetalleSolicitud());

            return new SolicitudDetalleResponseDto.DetalleActivoDto(
                    det.getIdDetalleSolicitud(),
                    idFallaReportada,
                    idTipoActivo,
                    codigoActivo,
                    descripcionActivo,
                    tipoActivo,
                    problemaEspecifico,
                    det.getEstadoDetSol(),
                    prioridad,
                    tieneAtencion,
                    informeEsp.isPresent(),
                    informeEsp.map(i -> i.getIdInformeEquipoEspecifico()).orElse(null)
            );
        }).toList();

        var informeGen = informeGenRepo.findBySolicitudIdSolicitud(sol.getIdSolicitud());

        return new SolicitudDetalleResponseDto(
                sol.getIdSolicitud(),
                sol.getNroSol() != null ? sol.getNroSol().toString() : "",
                sol.getNroSolExterna(),
                sol.getEstadoSol() != null ? sol.getEstadoSol().name() : "",
                sol.getFechaSol() != null ? sol.getFechaSol().toString() : "",
                sol.getDescripcionSol(),
                sol.getObservacionFicha(),
                solicitante,
                celular,
                correo,
                unidad,
                cargo,
                informeGen.isPresent(),
                informeGen.map(i -> i.getIdInformeEquipoGeneral()).orElse(null),
                activos
        );
    }

    @Override
    public List<Map<String, Object>> listaSolicitudFinalizado() {
        log.info("Lista de solicitudes finalizadas" + repo.listaSolicitudFinalizado());
        return repo.listaSolicitudFinalizado();
    }

    @Override
    public List<Map<String, Object>> listaSolicitudPorTecnico(String username) {
        return repo.listaSolicitudPorTecnico(username);
    }

    @Override
    public Long colaMax() {
        Long total = repo.colaMax();
        return total + 1;
    }

    @Override
    public List<Map<String, Object>> listaSolicitudServicioTecnico() {
        return repo.listaSolicitudServicioTecnico();
    }

    private String estadoDetalleParaSolicitud(EstadoSolicitudEnum estado) {
        return switch (estado) {
            case PENDIENTE -> "PENDIENTE";
            case EN_MANTENIMIENTO -> "EN_ATENCION";
            case FINALIZADO -> "RESUELTO";
        };
    }
}
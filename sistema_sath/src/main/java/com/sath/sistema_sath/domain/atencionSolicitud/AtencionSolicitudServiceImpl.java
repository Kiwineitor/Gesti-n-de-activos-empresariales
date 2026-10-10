package com.sath.sistema_sath.domain.atencionSolicitud;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.SolicitudLondra.LondraNotificacionService;
import com.sath.sistema_sath.tools.colas.ColaPriorizadaSolicitudes;
import com.sath.sistema_sath.tools.reportes.ReporteGenericoRepository;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.atencionSolicitud.Enum.EstadoAtencion;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.*;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitudRepository;
import com.sath.sistema_sath.domain.falla.Falla;
import com.sath.sistema_sath.domain.falla.FallaRepository;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportada;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportadaRepository;
import com.sath.sistema_sath.domain.prioridad.PrioridadRepository;
import com.sath.sistema_sath.domain.servicio.ServicioRepository;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudMapper;
import com.sath.sistema_sath.domain.tecnicoAsignado.TecnicoAsignado;
import com.sath.sistema_sath.domain.tecnicoAsignado.TecnicoAsignadoRepository;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtencionSolicitudServiceImpl implements AtencionSolicitudService {

    private final AtencionSolicitudRepository atencionRepo;
    private final AtencionServicioRepository atencionServicioRepo;
    private final ServicioRepository servicioRepo;
    private final PrioridadRepository prioridadRepo;
    private final DetalleSolicitudRepository detalleRepo;
    private final FallaReportadaRepository fallaReportadaRepo;
    private final FallaRepository fallaRepo;
    private final LondraNotificacionService londraNotificacionService;
    private final AtencionSolicitudMapper mapper;
    private final ColaPriorizadaSolicitudes colaPriorizada;
    private final SolicitudMapper solicitudMapper;
    private final ReporteGenericoRepository reporteGenericoRepository;
    private final TecnicoAsignadoRepository tecnicoAsignadoRepo;
    private final UsuarioRepository usuarioRepo;
    private final AdministrativoRepository adminRepo;

    @Override
    @Transactional
    public AtencionSolicitudResponseDto create(AtencionSolicitudCreateDto dto) {
        AtencionSolicitud entity = mapper.toEntity(dto);
        resolverRelaciones(entity, dto.getIdsServicios(), dto.getIdPrioridad(), dto.getIdDetalleSolicitud());
        AtencionSolicitud saved = atencionRepo.save(entity);

        agregarAColaPriorizada(saved);

        return mapper.toResponseDto(saved);
    }

    @Override
    public List<AtencionSolicitudResponseDto> findAll() {
        return mapper.toResponseDtoList(atencionRepo.findAll());
    }

    @Override
    public AtencionSolicitudResponseDto findById(Long id) {
        AtencionSolicitud entity = atencionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atención no encontrada con ID: " + id));
        return mapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public AtencionSolicitudResponseDto update(Long id, AtencionSolicitudUpdateDto dto) {
        AtencionSolicitud entity = atencionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atención no encontrada con ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        resolverRelaciones(entity, dto.getIdsServicios(), dto.getIdPrioridad(), dto.getIdDetalleSolicitud());
        AtencionSolicitud saved = atencionRepo.save(entity);

        // La prioridad puede haber cambiado — reposicionar en la cola
        agregarAColaPriorizada(saved);

        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AtencionSolicitud entity = atencionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atención no encontrada con ID: " + id));

        // Remover de la cola priorizada antes de marcar como eliminado
        quitarDeColaPriorizada(entity);

        entity.setEstado(EstadoEnum.ELIMINADO);
        atencionRepo.save(entity);
    }

    @Override
    public List<Map<String, Object>> listaParaElFrontend() {
        return atencionRepo.listaParaElFrontend();
    }

    @Override
    @Transactional
    public AtencionSolicitudResponseDto atender(AtenderDetalleDto dto, String username) {
        AtencionSolicitud atencion = new AtencionSolicitud();
        atencion.setEstadoReparacionAtenSol(EstadoAtencion.RECIBIDO);
        atencion.setFechIniReparacionAtenSol(LocalDate.now());
        resolverRelaciones(atencion, dto.idsServicios(), dto.idPrioridad(), dto.idDetalleSolicitud());
        AtencionSolicitud saved = atencionRepo.save(atencion);
        agregarAColaPriorizada(saved);
        autoAsignarTecnico(username, saved.getDetalleSolicitud().getSolicitud());
        if (dto.idsFallas() != null && !dto.idsFallas().isEmpty()) {
            for (int i = 0; i < dto.idsFallas().size(); i++) {
                final int idx = i;
                Falla falla = fallaRepo.findById(dto.idsFallas().get(i))
                        .orElseThrow(() -> new IllegalArgumentException("Falla no encontrada: " + dto.idsFallas().get(idx)));

                if (i == 0 && dto.idFallaReportada() != null) {
                    // Primera falla: cataloga la FallaReportada existente (la que vino de Londra)
                    FallaReportada fr = fallaReportadaRepo.findById(dto.idFallaReportada())
                            .orElseThrow(() -> new IllegalArgumentException("FallaReportada no encontrada: " + dto.idFallaReportada()));
                    fr.setFalla(falla);
                    fallaReportadaRepo.save(fr);
                    log.info("FallaReportada {} catalogada como '{}'", dto.idFallaReportada(), falla.getNombreFall());
                } else {
                    // Fallas adicionales: crear nuevas FallaReportada para el mismo DetalleSolicitud
                    FallaReportada nueva = new FallaReportada();
                    nueva.setFalla(falla);
                    nueva.setDetalleSolicitud(saved.getDetalleSolicitud());
                    fallaReportadaRepo.save(nueva);
                    log.info("FallaReportada adicional '{}' creada para detalle {}", falla.getNombreFall(), dto.idDetalleSolicitud());
                }
            }

            try {
                Solicitud solicitud = saved.getDetalleSolicitud().getSolicitud();
                if (solicitud != null && solicitud.getMantIdLondra() != null) {
                    Falla primera = fallaRepo.findById(dto.idsFallas().get(0)).orElse(null);
                    if (primera != null) {
                        int posicion = colaPriorizada.calcularPosicionGlobal(solicitud.getIdSolicitud());
                        londraNotificacionService.notificarCatalogacion(
                                solicitud.getMantIdLondra(), primera.getNombreFall(), posicion);
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo notificar catalogación a Londra: {}", e.getMessage());
            }
        }

        return mapper.toResponseDto(saved);
    }

    @Override
    public List<Map<String, Object>> listaAtenciones() {
        return reporteGenericoRepository.listaAtenciones();
    }

    @Override
    public List<Map<String, Object>> listaAtencionesPorTecnico(String username) {
        return reporteGenericoRepository.listaAtencionesPorTecnico(username);
    }

    @Override
    public List<Map<String, Object>> listaParaAsignar() {
        return atencionRepo.listaParaAsignar();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void resolverRelaciones(AtencionSolicitud entity, List<Long> servicioIds, Long prioridadId, Long detalleId) {
        if (servicioIds != null && !servicioIds.isEmpty()) {
            entity.getAtencionServicios().clear();
            for (Long servicioId : servicioIds) {
                var servicio = servicioRepo.findById(servicioId)
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + servicioId));
                AtencionServicio as = new AtencionServicio();
                as.setAtencionSolicitud(entity);
                as.setServicio(servicio);
                as.setFechaAsignacion(LocalDate.now());
                entity.getAtencionServicios().add(as);
            }
        }
        if (prioridadId != null) {
            entity.setPrioridad(prioridadRepo.findById(prioridadId)
                    .orElseThrow(() -> new IllegalArgumentException("Prioridad no encontrada: " + prioridadId)));
        }
        if (detalleId != null) {
            entity.setDetalleSolicitud(detalleRepo.findById(detalleId)
                    .orElseThrow(() -> new IllegalArgumentException("DetalleSolicitud no encontrado: " + detalleId)));
        }
    }

    private void agregarAColaPriorizada(AtencionSolicitud atencion) {
        if (atencion.getPrioridad() == null || atencion.getDetalleSolicitud() == null) return;

        Solicitud solicitud = atencion.getDetalleSolicitud().getSolicitud();
        if (solicitud == null) {
            log.warn("AtencionSolicitud id={} no tiene Solicitud asociada — no se agrega a la cola priorizada",
                    atencion.getIdAtencionSolicitud());
            return;
        }

        var prioridad = atencion.getPrioridad().getNivel();
        var solicitudDto = solicitudMapper.toResponseDto(solicitud);
        colaPriorizada.agregarOActualizar(solicitud.getIdSolicitud(), prioridad, solicitudDto);

        int posicion = colaPriorizada.calcularPosicionGlobal(solicitud.getIdSolicitud());
        log.info("Solicitud {} en cola priorizada con prioridad {} — posición global: {}",
                solicitud.getIdSolicitud(), prioridad, posicion);
    }

    private void quitarDeColaPriorizada(AtencionSolicitud atencion) {
        if (atencion.getDetalleSolicitud() == null) return;
        Solicitud solicitud = atencion.getDetalleSolicitud().getSolicitud();
        if (solicitud == null) return;
        colaPriorizada.remover(solicitud.getIdSolicitud());
        log.info("Solicitud {} removida de la cola priorizada (atención eliminada)", solicitud.getIdSolicitud());
    }

    private void autoAsignarTecnico(String username, Solicitud solicitud) {
        if (username == null || solicitud == null) return;
        try {
            Usuario usuario = usuarioRepo.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
            if (usuario.getPersona() == null) return;
            Administrativo admin = adminRepo.findFirstByPersona(usuario.getPersona()).orElse(null);
            if (admin == null) {
                log.warn("El usuario '{}' no tiene perfil administrativo — no se auto-asignó como técnico", username);
                return;
            }
            if (tecnicoAsignadoRepo.existsByAdministrativoAndSolicitud(admin, solicitud)) {
                log.debug("Técnico '{}' ya está asignado a solicitud {}", username, solicitud.getIdSolicitud());
                return;
            }
            TecnicoAsignado asignacion = new TecnicoAsignado();
            asignacion.setAdministrativo(admin);
            asignacion.setSolicitud(solicitud);
            asignacion.setFechaAsignacionTecAsig(LocalDate.now());
            tecnicoAsignadoRepo.save(asignacion);
            log.info("Técnico '{}' auto-asignado a solicitud {}", username, solicitud.getIdSolicitud());
        } catch (Exception e) {
            log.warn("No se pudo auto-asignar técnico '{}': {}", username, e.getMessage());
        }
    }
}

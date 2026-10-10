package com.sath.sistema_sath.SolicitudLondra;

import com.sath.sistema_sath.SolicitudLondra.dto.SolicitudLondraDTO;
import com.sath.sistema_sath.domain.activo.Activo;
import com.sath.sistema_sath.domain.activo.ActivoRepository;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.cargo.Cargo;
import com.sath.sistema_sath.domain.cargo.CargoRepository;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitudRepository;
import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportada;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportadaRepository;
import com.sath.sistema_sath.domain.persona.Persona;
import com.sath.sistema_sath.domain.persona.PersonaRepository;
import com.sath.sistema_sath.domain.solicitud.ColaSolicitudes;
import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudMapper;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudResponseDto;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivo;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivoRepository;
import com.sath.sistema_sath.domain.unidad.Unidad;
import com.sath.sistema_sath.domain.unidad.UnidadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MantenimientoService {

    private final SolicitudRepository solicitudRepo;
    private final DetalleSolicitudRepository detalleSolicitudRepo;
    private final ActivoRepository activoRepo;
    private final FallaReportadaRepository fallaReportadaRepo;
    private final AdministrativoRepository administrativoRepo;
    private final PersonaRepository personaRepo;
    private final UnidadRepository unidadRepo;
    private final CargoRepository cargoRepo;
    private final ColaSolicitudes colaSolicitudes;
    private final SolicitudMapper solicitudMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final TipoActivoRepository tipoActivoRepo;

    @Value("${solicitud.digitos.secuencia:6}")
    private int digitosSecuencia;

    public int procesarSolicitudLondra(SolicitudLondraDTO dto) {

        // Idempotencia: si ya existe una solicitud con ese id externo (idSolicitud de
        // Jhowil), ignorar reenvíos
        Integer idExterno = dto.getIdSolicitudLondra();
        if (idExterno != null && solicitudRepo.existsByMantIdLondra(idExterno)) {
            log.warn("Solicitud duplicada ignorada: idSolicitudLondra={}", idExterno);
            return -1;
        }

        // 1. Sincronizar o crear Administrativo y sus datos relacionados
        Administrativo admin = sincronizarAdministrativo(dto);

        // 2. Crear la Solicitud en SATH
        Solicitud solicitud = new Solicitud();
        solicitud.setAdministrativo(admin);
        solicitud.setDescripcionSol(dto.getDescripcion());
        solicitud.setEstadoSol(EstadoSolicitudEnum.PENDIENTE);
        solicitud.setFechaSol(parsearFechaLondra(dto.getFechaSolicitud()));
        solicitud.setGestionSol(solicitud.getFechaSol().getYear());
        solicitud.setNroSol(generarNroSolicitud());
        solicitud.setNroSolExterna(dto.getNumeroSolicitudLondra());
        solicitud.setMantIdLondra(dto.getIdSolicitudLondra());
        solicitudRepo.save(solicitud);
        // log.info("Solicitud SATH #{} creada para Londra id={}",
        // solicitud.getNroSol(), dto.getIdSolicitudLondra());

        // 3. Agregar a la cola FIFO y notificar posición a Londra y al frontend
        SolicitudResponseDto solicitudDto = solicitudMapper.toResponseDto(solicitud);
        colaSolicitudes.agregar(solicitudDto);
        int posicion = colaSolicitudes.getPosicion(solicitud.getIdSolicitud());

        try {
            messagingTemplate.convertAndSend("/topic/solicitudes/nueva", solicitudDto);
        } catch (Exception e) {
            log.warn("Error WebSocket al notificar solicitud {}: {}", solicitud.getIdSolicitud(), e.getMessage());
        }

        log.info("Solicitud en cola: id={} posicion={}", dto.getIdSolicitudLondra(), posicion);

        // 4. Por cada activo del detalle crear DetalleSolicitud y FallasReportadas
        if (dto.getEquipos() == null)
            return posicion;

        for (SolicitudLondraDTO.DetalleActivoLondraDTO det : dto.getEquipos()) {

            // Sincronizar TipoActivo por nombre
            TipoActivo tipoActivo = null;
            if (det.getTipoActivo() != null && !det.getTipoActivo().isBlank()) {
                tipoActivo = tipoActivoRepo.findByNombreTipoAct(det.getTipoActivo().trim()).orElseGet(() -> {
                    TipoActivo nuevo = new TipoActivo();
                    nuevo.setNombreTipoAct(det.getTipoActivo().trim());
                    return tipoActivoRepo.save(nuevo);
                });
            }

            // Buscar o crear Activo por código, asignando TipoActivo
            final TipoActivo tipoActivoFinal = tipoActivo;
            Activo activo = activoRepo.findByCodigoAct(det.getCodigoActivo().trim()).orElseGet(() -> {
                Activo nuevo = new Activo();
                nuevo.setCodigoAct(det.getCodigoActivo().trim());
                nuevo.setDescripcionAct(det.getDescripcionActivo());
                nuevo.setTipoActivo(tipoActivoFinal);
                return activoRepo.save(nuevo);
            });

            // Si el activo ya existía pero no tenía TipoActivo asignado, actualizarlo
            if (activo.getTipoActivo() == null && tipoActivo != null) {
                activo.setTipoActivo(tipoActivo);
                activoRepo.save(activo);
            }

            // Crear DetalleSolicitud
            DetalleSolicitud detalle = new DetalleSolicitud();
            detalle.setSolicitud(solicitud);
            detalle.setActivo(activo);
            detalle.setEstadoDetSol("PENDIENTE");
            detalle.setFechaActualizacionDetSol(LocalDate.now());
            detalle.setTipoInforme(TipoInformeEnum.GENERAL);
            detalleSolicitudRepo.save(detalle);

            // FallaReportada con el problemaEspecifico como descripción libre
            if (det.getProblemaEspecifico() != null && !det.getProblemaEspecifico().isBlank()) {
                FallaReportada fr = new FallaReportada();
                fr.setDetalleSolicitud(detalle);
                fr.setFalla(null);
                fr.setDescripcionFallRep(det.getProblemaEspecifico().trim());
                fallaReportadaRepo.save(fr);
            }
        }
        return posicion;
    }

    private Administrativo sincronizarAdministrativo(SolicitudLondraDTO dto) {
        SolicitudLondraDTO.FuncionarioLondraDTO fun = dto.getFuncionario();

        // Persona
        Persona persona = personaRepo.findByCiPer(fun.getCi()).orElseGet(() -> {
            Persona nueva = new Persona();
            nueva.setCiPer(fun.getCi());
            return nueva;
        });
        persona.setNombrePer(fun.getNombre());
        persona.setApPaternoPer(fun.getPaterno());
        persona.setApMaternoPer(fun.getMaterno());
        persona.setNroCellPer(fun.getCelular());
        persona.setCorreoPer(fun.getCorreo());
        personaRepo.save(persona);

        // Unidad
        Unidad unidad = unidadRepo.findByNombreUni(fun.getNombreUnidad()).orElseGet(() -> {
            Unidad nueva = new Unidad();
            nueva.setNombreUni(fun.getNombreUnidad());
            return unidadRepo.save(nueva);
        });

        // Cargo
        Cargo cargo = cargoRepo.findByNombreCar(fun.getCargoSolicitante()).orElseGet(() -> {
            Cargo nuevo = new Cargo();
            nuevo.setNombreCar(fun.getCargoSolicitante());
            return cargoRepo.save(nuevo);
        });

        // Administrativo
        Administrativo admin = administrativoRepo.findByCodigoAdmi(fun.getCodigoAdministrativo()).orElseGet(() -> {
            Administrativo nuevo = new Administrativo();
            nuevo.setCodigoAdmi(fun.getCodigoAdministrativo());
            return nuevo;
        });
        admin.setPersona(persona);
        admin.setUnidad(unidad);
        admin.setCargo(cargo);
        return administrativoRepo.save(admin);
    }

    private Long generarNroSolicitud() {
        int anio = LocalDate.now().getYear();
        long count = solicitudRepo.countByGestionSol(anio) + 1;
        String formato = "%d%0" + digitosSecuencia + "d";
        return Long.parseLong(String.format(formato, anio, count));
    }

    private LocalDate parsearFechaLondra(String fechaLondra) {
        if (fechaLondra == null || fechaLondra.isBlank())
            return LocalDate.now();
        String f = fechaLondra.trim();
        // "dd/MM/yyyy HH:mm:ss" — formato nuevo de Londra
        try {
            return LocalDateTime.parse(f, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        // "yyyy-MM-dd" — ISO
        try {
            return LocalDate.parse(f, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
        }
        // "M/d/yy" — formato antiguo
        try {
            return LocalDate.parse(f, DateTimeFormatter.ofPattern("M/d/yy"));
        } catch (DateTimeParseException ignored) {
        }
        log.warn("Fecha no parseable '{}', usando fecha actual", f);
        return LocalDate.now();
    }
}

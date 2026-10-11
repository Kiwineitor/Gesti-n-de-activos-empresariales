package com.sath.sistema_sath.domain.solicitud.manual;

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
import com.sath.sistema_sath.domain.solicitud.EstadoCambiadoEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SolicitudManualService {

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
    private final TipoActivoRepository tipoActivoRepo;
    private final ApplicationEventPublisher publisher;

    @Value("${solicitud.digitos.secuencia:6}")
    private int digitosSecuencia;

    public SolicitudResponseDto registrar(SolicitudManualCreateDto dto) {

        // 1. Sincronizar o crear Administrativo y sus datos relacionados
        Administrativo admin = sincronizarAdministrativo(dto.funcionario());

        // 2. Crear la Solicitud en SATH
        LocalDate hoy = dto.fechaSol() != null ? dto.fechaSol() : LocalDate.now();
        Solicitud solicitud = new Solicitud();
        solicitud.setAdministrativo(admin);
        solicitud.setDescripcionSol(dto.descripcion());
        solicitud.setEstadoSol(EstadoSolicitudEnum.PENDIENTE);
        solicitud.setFechaSol(hoy);
        solicitud.setGestionSol(hoy.getYear());
        solicitud.setNroSol(generarNroSolicitud());
        solicitud.setIdExterno(null);
        solicitud.setNroSolExterna(null);
        solicitudRepo.save(solicitud);

        // 3. Mapear, agregar a cola y obtener posición
        SolicitudResponseDto responseDto = solicitudMapper.toResponseDto(solicitud);
        colaSolicitudes.agregar(responseDto);
        int posicion = colaSolicitudes.getPosicion(solicitud.getIdSolicitud());

        // 4. Publicar evento WebSocket
        publisher.publishEvent(new EstadoCambiadoEvent(responseDto, EstadoSolicitudEnum.PENDIENTE));

        log.info("Solicitud manual creada: id={} posicion={}", solicitud.getIdSolicitud(), posicion);

        // 5. Por cada activo del detalle crear DetalleSolicitud y FallaReportada
        if (dto.equipos() != null) {
            for (ActivoManualDto det : dto.equipos()) {

                // Sincronizar TipoActivo por nombre
                TipoActivo tipoActivo = null;
                if (det.tipoActivo() != null && !det.tipoActivo().isBlank()) {
                    tipoActivo = tipoActivoRepo.findByNombreTipoAct(det.tipoActivo().trim()).orElseGet(() -> {
                        TipoActivo nuevo = new TipoActivo();
                        nuevo.setNombreTipoAct(det.tipoActivo().trim());
                        return tipoActivoRepo.save(nuevo);
                    });
                }

                // Buscar o crear Activo por código
                final TipoActivo tipoActivoFinal = tipoActivo;
                Activo activo = activoRepo.findByCodigoAct(det.codigoActivo().trim()).orElseGet(() -> {
                    Activo nuevo = new Activo();
                    nuevo.setCodigoAct(det.codigoActivo().trim());
                    nuevo.setDescripcionAct(det.descripcionActivo());
                    nuevo.setTipoActivo(tipoActivoFinal);
                    return activoRepo.save(nuevo);
                });

                // Si el activo ya existía pero sin TipoActivo, actualizarlo
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
                if (det.problemaEspecifico() != null && !det.problemaEspecifico().isBlank()) {
                    FallaReportada fr = new FallaReportada();
                    fr.setDetalleSolicitud(detalle);
                    fr.setFalla(null);
                    fr.setDescripcionFallRep(det.problemaEspecifico().trim());
                    fallaReportadaRepo.save(fr);
                }
            }
        }

        // 6. Retornar DTO final con posicionCola correcta
        return new SolicitudResponseDto(
                responseDto.idSolicitud(),
                responseDto.estadoSol(),
                responseDto.nroSol(),
                responseDto.gestionSol(),
                responseDto.fechaSol(),
                responseDto.descripcionSol(),
                responseDto.observacionFicha(),
                responseDto.idExterno(),
                responseDto.nroSolExterna(),
                posicion
        );
    }

    private Optional<Persona> buscarPersonaPorCi(String ci) {
        if (ci == null || ci.trim().isBlank()) {
            return Optional.empty();
        }
        return personaRepo.findByCiPer(ci.trim());
    }

    private Optional<Administrativo> buscarAdministrativoPorCodigo(Integer codigoAdmi) {
        if (codigoAdmi == null) {
            return Optional.empty();
        }
        return administrativoRepo.findByCodigoAdmi(codigoAdmi);
    }


    private Administrativo sincronizarAdministrativo(FuncionarioManualDto fun) {
        String ci = (fun.ci() != null && !fun.ci().trim().isBlank()) ? fun.ci().trim() : null;
        Integer codigoAdmi = fun.codigoAdministrativo();

        // Filtro 1: Búsqueda por CI
        Optional<Persona> personaPorCi = buscarPersonaPorCi(ci);

        // Filtro 2: Búsqueda por Código Administrativo
        Optional<Administrativo> adminPorCodigo = buscarAdministrativoPorCodigo(codigoAdmi);

        // Resolver o crear Persona
        Persona persona;
        if (personaPorCi.isPresent()) {
            persona = personaPorCi.get();
        } else if (adminPorCodigo.isPresent() && adminPorCodigo.get().getPersona() != null) {
            persona = adminPorCodigo.get().getPersona();
        } else {
            persona = new Persona();
        }

        if (ci != null) {
            persona.setCiPer(ci);
        }
        if (fun.nombre() != null && !fun.nombre().trim().isBlank()) {
            persona.setNombrePer(fun.nombre().trim());
        }
        if (fun.paterno() != null && !fun.paterno().trim().isBlank()) {
            persona.setApPaternoPer(fun.paterno().trim());
        }
        if (fun.materno() != null) {
            persona.setApMaternoPer(fun.materno().trim());
        }
        if (fun.celular() != null) {
            persona.setNroCellPer(fun.celular().trim());
        }
        if (fun.correo() != null && !fun.correo().trim().isBlank()) {
            persona.setCorreoPer(fun.correo().trim());
        }
        persona = personaRepo.save(persona);

        // Resolver Unidad
        Unidad unidad = null;
        if (fun.nombreUnidad() != null && !fun.nombreUnidad().trim().isBlank()) {
            String nombreUni = fun.nombreUnidad().trim();
            unidad = unidadRepo.findByNombreUni(nombreUni).orElseGet(() -> {
                Unidad nueva = new Unidad();
                nueva.setNombreUni(nombreUni);
                return unidadRepo.save(nueva);
            });
        }

        // Resolver Cargo
        Cargo cargo = null;
        if (fun.cargoSolicitante() != null && !fun.cargoSolicitante().trim().isBlank()) {
            String nombreCar = fun.cargoSolicitante().trim();
            cargo = cargoRepo.findByNombreCar(nombreCar).orElseGet(() -> {
                Cargo nuevo = new Cargo();
                nuevo.setNombreCar(nombreCar);
                return cargoRepo.save(nuevo);
            });
        }

        // Resolver Administrativo
        Administrativo admin;
        if (adminPorCodigo.isPresent()) {
            admin = adminPorCodigo.get();
        } else {
            admin = administrativoRepo.findFirstByPersona(persona).orElseGet(Administrativo::new);
            if (codigoAdmi != null) {
                admin.setCodigoAdmi(codigoAdmi);
            }
        }

        admin.setPersona(persona);
        if (unidad != null) {
            admin.setUnidad(unidad);
        }
        if (cargo != null) {
            admin.setCargo(cargo);
        }
        return administrativoRepo.save(admin);
    }

    private Long generarNroSolicitud() {
        int anio = LocalDate.now().getYear();
        long count = solicitudRepo.countByGestionSol(anio) + 1;
        String formato = "%d%0" + digitosSecuencia + "d";
        return Long.parseLong(String.format(formato, anio, count));
    }
}

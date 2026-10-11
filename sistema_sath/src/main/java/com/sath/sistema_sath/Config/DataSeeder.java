package com.sath.sistema_sath.Config;

import com.sath.sistema_sath.domain.activo.Activo;
import com.sath.sistema_sath.domain.activo.ActivoRepository;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionServicio;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionServicioRepository;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionSolicitud;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionSolicitudRepository;
import com.sath.sistema_sath.domain.atencionSolicitud.Enum.EstadoAtencion;
import com.sath.sistema_sath.domain.cargo.Cargo;
import com.sath.sistema_sath.domain.cargo.CargoRepository;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitudRepository;
import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;
import com.sath.sistema_sath.domain.falla.Falla;
import com.sath.sistema_sath.domain.falla.FallaRepository;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportada;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportadaRepository;
import com.sath.sistema_sath.domain.permiso.Permiso;
import com.sath.sistema_sath.domain.permiso.PermisoRepository;
import com.sath.sistema_sath.domain.persona.Persona;
import com.sath.sistema_sath.domain.persona.PersonaRepository;
import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;
import com.sath.sistema_sath.domain.prioridad.Prioridad;
import com.sath.sistema_sath.domain.prioridad.PrioridadRepository;
import com.sath.sistema_sath.domain.rol.Rol;
import com.sath.sistema_sath.domain.rol.RolRepository;
import com.sath.sistema_sath.domain.rolPermiso.RolPermiso;
import com.sath.sistema_sath.domain.rolPermiso.RolPermisoRepository;
import com.sath.sistema_sath.domain.servicio.Servicio;
import com.sath.sistema_sath.domain.servicio.ServicioRepository;
import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import com.sath.sistema_sath.domain.tecnicoAsignado.TecnicoAsignado;
import com.sath.sistema_sath.domain.tecnicoAsignado.TecnicoAsignadoRepository;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivo;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivoRepository;
import com.sath.sistema_sath.domain.tipoServicio.TipoServicio;
import com.sath.sistema_sath.domain.tipoServicio.TipoServicioRepository;
import com.sath.sistema_sath.domain.unidad.Unidad;
import com.sath.sistema_sath.domain.unidad.UnidadRepository;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import com.sath.sistema_sath.domain.usrRol.UsrRol;
import com.sath.sistema_sath.domain.usrRol.UsrRolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga inicial de la base de datos (solo si está vacía).
 * <ul>
 *   <li>Siempre: roles, permisos, prioridades, unidad de Soporte Técnico y el usuario administrador.</li>
 *   <li>Si {@code app.demo-data=true}: usuarios de prueba por rol, catálogos (recursos, fallas,
 *       servicios) y solicitudes en todos los estados para poder recorrer el sistema.</li>
 * </ul>
 * Se ejecuta después de {@code TipoServicioSeeder} (Order 10) y antes de {@code ColaInicializador}.
 */
@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CargoRepository cargoRepository;
    private final UnidadRepository unidadRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsrRolRepository usrRolRepository;
    private final AdministrativoRepository administrativoRepository;
    private final PrioridadRepository prioridadRepository;
    private final TipoActivoRepository tipoActivoRepository;
    private final ActivoRepository activoRepository;
    private final FallaRepository fallaRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final ServicioRepository servicioRepository;
    private final SolicitudRepository solicitudRepository;
    private final DetalleSolicitudRepository detalleSolicitudRepository;
    private final FallaReportadaRepository fallaReportadaRepository;
    private final AtencionSolicitudRepository atencionSolicitudRepository;
    private final AtencionServicioRepository atencionServicioRepository;
    private final TecnicoAsignadoRepository tecnicoAsignadoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo-data:true}")
    private boolean demoData;

    @Value("${solicitud.digitos.secuencia:6}")
    private int digitosSecuencia;

    private final Map<String, Cargo> cargos = new HashMap<>();
    private final Map<String, Unidad> unidades = new HashMap<>();
    private final Map<String, Rol> roles = new HashMap<>();
    private final Map<String, Prioridad> prioridades = new HashMap<>();
    private final Map<Integer, Integer> secuenciaPorGestion = new HashMap<>();
    private int codigoAdministrativo = 1000;

    @Override
    public void run(String... args) {

        if (rolRepository.count() > 0) {
            log.info("DataSeeder omitido — la base de datos ya contiene datos.");
            return;
        }

        log.info("Iniciando carga de datos iniciales...");
        cargarDatosBase();

        if (demoData) {
            cargarDatosDemo();
            log.info("DataSeeder completado — datos base y de demostración cargados.");
        } else {
            log.info("DataSeeder completado — datos base cargados (app.demo-data=false).");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  DATOS BASE
    // ════════════════════════════════════════════════════════════════════

    private void cargarDatosBase() {

        // ── 1. CARGOS ──────────────────────────────────────────────
        for (String nombre : List.of("Administrador del Sistema", "Técnico de Soporte", "Supervisor",
                "Jefe de Área", "Analista", "Asistente")) {
            Cargo c = new Cargo();
            c.setNombreCar(nombre);
            cargos.put(nombre, cargoRepository.save(c));
        }

        // ── 2. UNIDADES ────────────────────────────────────────────
        // "SOPORTE TÉCNICO" es la unidad responsable; la configuración de reportes se guarda en ella.
        for (String nombre : List.of("SOPORTE TÉCNICO", "ADMINISTRACIÓN", "RECURSOS HUMANOS",
                "CONTABILIDAD", "VENTAS", "LOGÍSTICA")) {
            Unidad u = new Unidad();
            u.setNombreUni(nombre);
            unidades.put(nombre, unidadRepository.save(u));
        }

        // ── 3. ROLES ───────────────────────────────────────────────
        for (String nombre : List.of("ADMINISTRADOR", "TECNICO", "USUARIO", "SUPERVISOR", "DEVELOPER")) {
            roles.put(nombre, rolRepository.save(new Rol(nombre)));
        }

        // ── 4. PERMISOS ────────────────────────────────────────────
        // Permisos funcionales
        Permiso pGestionarUsuarios    = new Permiso("GESTIONAR_USUARIOS");
        Permiso pGestionarRoles       = new Permiso("GESTIONAR_ROLES");
        Permiso pVerSolicitudes       = new Permiso("VER_SOLICITUDES");
        Permiso pGestionarSolicitudes = new Permiso("GESTIONAR_SOLICITUDES");
        Permiso pAtenderSolicitudes   = new Permiso("ATENDER_SOLICITUDES");
        Permiso pVerReportes          = new Permiso("VER_REPORTES");
        Permiso pGestionarActivos     = new Permiso("GESTIONAR_ACTIVOS");
        Permiso pGestionarFallas      = new Permiso("GESTIONAR_FALLAS");
        // Permisos de módulo (controlan visibilidad en el sidebar)
        Permiso pModHistorial         = new Permiso("MODULO_HISTORIAL");
        Permiso pModReportes          = new Permiso("MODULO_REPORTES");
        Permiso pModServicios         = new Permiso("MODULO_SERVICIOS");
        Permiso pModAdministrativos   = new Permiso("MODULO_ADMINISTRATIVOS");
        Permiso pModTiposServicio     = new Permiso("MODULO_TIPOS_SERVICIO");
        Permiso pModFallas            = new Permiso("MODULO_FALLAS");
        Permiso pModTecnicos          = new Permiso("MODULO_TECNICOS");
        Permiso pModUsuarios          = new Permiso("MODULO_USUARIOS");
        Permiso pModTiposActivo       = new Permiso("MODULO_TIPOS_ACTIVO");
        Permiso pModSolicitudes       = new Permiso("MODULO_SOLICITUDES");
        Permiso pModAtenciones        = new Permiso("MODULO_ATENCIONES");

        List<Permiso> todosLosPermisos = List.of(
            pGestionarUsuarios, pGestionarRoles, pVerSolicitudes, pGestionarSolicitudes,
            pAtenderSolicitudes, pVerReportes, pGestionarActivos, pGestionarFallas,
            pModHistorial, pModReportes, pModServicios, pModAdministrativos,
            pModTiposServicio, pModFallas, pModTecnicos, pModUsuarios,
            pModTiposActivo, pModSolicitudes, pModAtenciones
        );
        permisoRepository.saveAll(todosLosPermisos);

        // ── 5. ROL_PERMISO ─────────────────────────────────────────
        // ADMINISTRADOR y DEVELOPER → acceso total
        for (Permiso p : todosLosPermisos) {
            asignarPermiso(roles.get("ADMINISTRADOR"), p);
            asignarPermiso(roles.get("DEVELOPER"), p);
        }
        // TECNICO → atenciones, solicitudes, historial, reportes, técnicos
        for (Permiso p : List.of(pVerSolicitudes, pAtenderSolicitudes, pVerReportes,
                                  pModAtenciones, pModHistorial, pModReportes, pModTecnicos)) {
            asignarPermiso(roles.get("TECNICO"), p);
        }
        // SUPERVISOR → ver solicitudes y reportes
        for (Permiso p : List.of(pVerSolicitudes, pVerReportes, pModSolicitudes, pModReportes)) {
            asignarPermiso(roles.get("SUPERVISOR"), p);
        }
        // USUARIO → solo crear y ver solicitudes
        for (Permiso p : List.of(pVerSolicitudes, pGestionarSolicitudes, pModSolicitudes)) {
            asignarPermiso(roles.get("USUARIO"), p);
        }

        // ── 6. PRIORIDADES ─────────────────────────────────────────
        int orden = 1;
        for (PrioridadEnum nivel : List.of(PrioridadEnum.ALTO, PrioridadEnum.MEDIO, PrioridadEnum.BAJO)) {
            Prioridad p = new Prioridad();
            p.setNivel(nivel);
            p.setOrdenPri(orden++);
            prioridades.put(nivel.name(), prioridadRepository.save(p));
        }

        // ── 7. USUARIO ADMINISTRADOR ───────────────────────────────
        crearUsuario("admin", "admin123", "10000001", "Laura", "Méndez", "Ríos",
                "admin@demo.local", "SOPORTE TÉCNICO", "Administrador del Sistema",
                "ADMINISTRADOR", "DEVELOPER");
    }

    // ════════════════════════════════════════════════════════════════════
    //  DATOS DE DEMOSTRACIÓN
    // ════════════════════════════════════════════════════════════════════

    private void cargarDatosDemo() {

        // ── Usuarios de prueba (uno por rol) ───────────────────────
        Administrativo tecCarlos = crearUsuario("tecnico", "tecnico123", "10000002", "Carlos", "Rojas", "Vega",
                "tecnico@demo.local", "SOPORTE TÉCNICO", "Técnico de Soporte", "TECNICO");
        Administrativo tecDiego = crearUsuario("tecnico2", "tecnico123", "10000003", "Diego", "Vargas", "Luna",
                "tecnico2@demo.local", "SOPORTE TÉCNICO", "Técnico de Soporte", "TECNICO");
        crearUsuario("supervisor", "supervisor123", "10000004", "María", "Fernández", "Soto",
                "supervisor@demo.local", "ADMINISTRACIÓN", "Supervisor", "SUPERVISOR");
        Administrativo usrJorge = crearUsuario("usuario", "usuario123", "10000005", "Jorge", "Pérez", "Castro",
                "usuario@demo.local", "VENTAS", "Analista", "USUARIO");

        // ── Solicitantes (personal sin cuenta en el sistema) ───────
        Administrativo solAna   = crearAdministrativo("20000001", "Ana", "Torres", "Paz", "ana.torres@demo.local", "RECURSOS HUMANOS", "Jefe de Área");
        Administrativo solLuis  = crearAdministrativo("20000002", "Luis", "Gómez", "Rivera", "luis.gomez@demo.local", "CONTABILIDAD", "Analista");
        Administrativo solSofia = crearAdministrativo("20000003", "Sofía", "Morales", "Díaz", "sofia.morales@demo.local", "LOGÍSTICA", "Asistente");
        Administrativo solPablo = crearAdministrativo("20000004", "Pablo", "Herrera", "Quispe", "pablo.herrera@demo.local", "ADMINISTRACIÓN", "Asistente");

        // ── Tipos de recurso y fallas del catálogo ─────────────────
        Map<String, List<String>> catalogo = new LinkedHashMap<>();
        catalogo.put("Computadora",        List.of("No enciende", "Funcionamiento lento", "Pantalla sin imagen", "Virus o software malicioso"));
        catalogo.put("Impresora",          List.of("Atasco de papel", "Impresión borrosa", "No reconoce el cartucho"));
        catalogo.put("Proyector",          List.of("Lámpara quemada", "Imagen distorsionada"));
        catalogo.put("Vehículo",           List.of("Falla en frenos", "Mantenimiento de kilometraje", "Batería descargada"));
        catalogo.put("Mobiliario",         List.of("Pieza rota", "Cerradura dañada"));
        catalogo.put("Aire acondicionado", List.of("No enfría", "Fuga de agua", "Ruido excesivo"));

        Map<String, TipoActivo> tipos = new HashMap<>();
        Map<String, Falla> fallas = new HashMap<>();
        catalogo.forEach((tipoNombre, nombresFalla) -> {
            TipoActivo t = new TipoActivo();
            t.setNombreTipoAct(tipoNombre);
            tipos.put(tipoNombre, tipoActivoRepository.save(t));
            for (String nf : nombresFalla) {
                Falla f = new Falla();
                f.setNombreFall(nf);
                f.setTipoActivo(tipos.get(tipoNombre));
                fallas.put(nf, fallaRepository.save(f));
            }
        });

        // ── Recursos ───────────────────────────────────────────────
        Map<String, Activo> recursos = new LinkedHashMap<>();
        Object[][] datosRecursos = {
                {"REC-0001", "Computadora de escritorio - Recepción",        "Computadora"},
                {"REC-0002", "Laptop - Gerencia de Ventas",                  "Computadora"},
                {"REC-0003", "Computadora de escritorio - Contabilidad",     "Computadora"},
                {"REC-0004", "Impresora multifunción - Piso 1",              "Impresora"},
                {"REC-0005", "Impresora láser - Recursos Humanos",           "Impresora"},
                {"REC-0006", "Proyector - Sala de reuniones",                "Proyector"},
                {"REC-0007", "Camioneta de reparto - Placa 1234-ABC",        "Vehículo"},
                {"REC-0008", "Escritorio ejecutivo - Administración",        "Mobiliario"},
                {"REC-0009", "Aire acondicionado split - Oficina principal", "Aire acondicionado"},
                {"REC-0010", "Laptop - Logística",                           "Computadora"},
        };
        for (Object[] r : datosRecursos) {
            Activo a = new Activo();
            a.setCodigoAct((String) r[0]);
            a.setDescripcionAct((String) r[1]);
            a.setTipoActivo(tipos.get((String) r[2]));
            recursos.put((String) r[0], activoRepository.save(a));
        }

        // ── Servicios (por tipo de servicio, creados por TipoServicioSeeder) ──
        Map<String, TipoServicio> tiposServicio = new HashMap<>();
        tipoServicioRepository.findAll().forEach(ts -> tiposServicio.put(ts.getNombreTipo(), ts));
        Map<String, Servicio> servicios = new HashMap<>();
        Object[][] datosServicios = {
                {"Limpieza general",            "Limpieza interna y externa del recurso",          "PREVENTIVO"},
                {"Revisión programada",         "Inspección periódica según calendario",           "PREVENTIVO"},
                {"Cambio de pieza",             "Sustitución de un componente dañado",             "CORRECTIVO"},
                {"Reparación general",          "Diagnóstico y reparación de la falla reportada",  "CORRECTIVO"},
                {"Reinstalación de software",   "Formateo y reinstalación del sistema",            "CORRECTIVO"},
                {"Configuración",               "Ajustes de configuración y puesta en marcha",     "SERVICIO TÉCNICO"},
                {"Asesoramiento al usuario",    "Orientación sobre el uso correcto del recurso",   "SERVICIO TÉCNICO"},
        };
        for (Object[] s : datosServicios) {
            Servicio srv = new Servicio();
            srv.setNombreServ((String) s[0]);
            srv.setObservacionesServ((String) s[1]);
            srv.setTipoServicio(tiposServicio.get((String) s[2]));
            servicios.put((String) s[0], servicioRepository.save(srv));
        }

        LocalDate hoy = LocalDate.now();

        // ── Solicitudes FINALIZADAS (historial para reportes y dashboard) ──
        crearSolicitud(solAna, hoy.minusDays(75), EstadoSolicitudEnum.FINALIZADO,
                "La impresora del área atasca el papel constantemente.",
                recursos.get("REC-0005"), "Se atasca el papel en cada impresión", fallas.get("Atasco de papel"),
                PrioridadEnum.MEDIO, tecCarlos, List.of(servicios.get("Limpieza general"), servicios.get("Cambio de pieza")));
        crearSolicitud(solLuis, hoy.minusDays(52), EstadoSolicitudEnum.FINALIZADO,
                "La computadora de contabilidad está muy lenta.",
                recursos.get("REC-0003"), "Tarda varios minutos en abrir los programas", fallas.get("Funcionamiento lento"),
                PrioridadEnum.BAJO, tecDiego, List.of(servicios.get("Reinstalación de software"), servicios.get("Configuración")));
        crearSolicitud(solSofia, hoy.minusDays(30), EstadoSolicitudEnum.FINALIZADO,
                "La camioneta de reparto requiere mantenimiento.",
                recursos.get("REC-0007"), "Se cumplió el kilometraje para mantenimiento", fallas.get("Mantenimiento de kilometraje"),
                PrioridadEnum.MEDIO, tecCarlos, List.of(servicios.get("Revisión programada")));
        crearSolicitud(usrJorge, hoy.minusDays(14), EstadoSolicitudEnum.FINALIZADO,
                "El proyector de la sala de reuniones no muestra imagen.",
                recursos.get("REC-0006"), "Enciende pero no proyecta", fallas.get("Lámpara quemada"),
                PrioridadEnum.ALTO, tecDiego, List.of(servicios.get("Cambio de pieza"), servicios.get("Asesoramiento al usuario")));

        // ── Solicitudes EN MANTENIMIENTO (en proceso de atención) ──
        crearSolicitud(solPablo, hoy.minusDays(4), EstadoSolicitudEnum.EN_MANTENIMIENTO,
                "El aire acondicionado de la oficina principal no enfría.",
                recursos.get("REC-0009"), "Solo expulsa aire caliente", fallas.get("No enfría"),
                PrioridadEnum.ALTO, tecCarlos, List.of(servicios.get("Reparación general")));
        crearSolicitud(usrJorge, hoy.minusDays(2), EstadoSolicitudEnum.EN_MANTENIMIENTO,
                "La laptop de ventas no enciende.",
                recursos.get("REC-0002"), "No da señales de encendido", fallas.get("No enciende"),
                PrioridadEnum.MEDIO, tecDiego, List.of(servicios.get("Reparación general")));

        // ── Solicitudes PENDIENTES (en cola, sin atender) ──────────
        crearSolicitud(solAna, hoy.minusDays(1), EstadoSolicitudEnum.PENDIENTE,
                "La impresora multifunción imprime borroso.",
                recursos.get("REC-0004"), "Las hojas salen con manchas", null, null, null, List.of());
        crearSolicitud(solPablo, hoy, EstadoSolicitudEnum.PENDIENTE,
                "La cerradura del escritorio está dañada.",
                recursos.get("REC-0008"), "La llave no gira", null, null, null, List.of());
        crearSolicitud(solSofia, hoy, EstadoSolicitudEnum.PENDIENTE,
                "La laptop de logística muestra mensajes extraños.",
                recursos.get("REC-0010"), "Aparecen ventanas emergentes al navegar", null, null, null, List.of());
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════

    private void asignarPermiso(Rol rol, Permiso permiso) {
        RolPermiso rp = new RolPermiso();
        rp.setRol(rol);
        rp.setPermiso(permiso);
        rolPermisoRepository.save(rp);
    }

    private Administrativo crearAdministrativo(String ci, String nombre, String paterno, String materno,
                                               String correo, String unidad, String cargo) {
        Persona p = new Persona();
        p.setCiPer(ci);
        p.setNombrePer(nombre);
        p.setApPaternoPer(paterno);
        p.setApMaternoPer(materno);
        p.setNroCellPer("7" + ci.substring(ci.length() - 7));
        p.setCorreoPer(correo);
        personaRepository.save(p);

        Administrativo adm = new Administrativo();
        adm.setCodigoAdmi(++codigoAdministrativo);
        adm.setPersona(p);
        adm.setUnidad(unidades.get(unidad));
        adm.setCargo(cargos.get(cargo));
        return administrativoRepository.save(adm);
    }

    private Administrativo crearUsuario(String username, String password, String ci, String nombre,
                                        String paterno, String materno, String correo,
                                        String unidad, String cargo, String... nombresRol) {
        Administrativo adm = crearAdministrativo(ci, nombre, paterno, materno, correo, unidad, cargo);

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setPersona(adm.getPersona());
        usuarioRepository.save(u);

        for (String r : nombresRol) {
            UsrRol ur = new UsrRol();
            ur.setUsuario(u);
            ur.setRol(roles.get(r));
            usrRolRepository.save(ur);
        }
        return adm;
    }

    /**
     * Crea una solicitud con un recurso. Según el estado agrega la atención, el técnico,
     * la falla catalogada y los servicios realizados.
     */
    private void crearSolicitud(Administrativo solicitante, LocalDate fecha, EstadoSolicitudEnum estado,
                                String descripcion, Activo recurso, String problema, Falla fallaCatalogada,
                                PrioridadEnum prioridad, Administrativo tecnico, List<Servicio> serviciosRealizados) {
        Solicitud s = new Solicitud();
        s.setAdministrativo(solicitante);
        s.setDescripcionSol(descripcion);
        s.setEstadoSol(estado);
        s.setFechaSol(fecha);
        s.setGestionSol(fecha.getYear());
        s.setNroSol(siguienteNroSolicitud(fecha.getYear()));
        solicitudRepository.save(s);

        DetalleSolicitud d = new DetalleSolicitud();
        d.setSolicitud(s);
        d.setActivo(recurso);
        d.setEstadoDetSol(estado == EstadoSolicitudEnum.FINALIZADO ? "FINALIZADO"
                : estado == EstadoSolicitudEnum.EN_MANTENIMIENTO ? "EN_MANTENIMIENTO" : "PENDIENTE");
        d.setFechaActualizacionDetSol(fecha);
        d.setTipoInforme(TipoInformeEnum.GENERAL);
        detalleSolicitudRepository.save(d);

        FallaReportada fr = new FallaReportada();
        fr.setDetalleSolicitud(d);
        fr.setDescripcionFallRep(problema);
        fr.setFalla(estado == EstadoSolicitudEnum.PENDIENTE ? null : fallaCatalogada);
        fallaReportadaRepository.save(fr);

        if (estado == EstadoSolicitudEnum.PENDIENTE) return;

        boolean finalizada = estado == EstadoSolicitudEnum.FINALIZADO;
        AtencionSolicitud at = new AtencionSolicitud();
        at.setDetalleSolicitud(d);
        at.setPrioridad(prioridades.get(prioridad.name()));
        at.setEstadoReparacionAtenSol(finalizada ? EstadoAtencion.FINALIZADO : EstadoAtencion.TALLER);
        at.setFechIniReparacionAtenSol(fecha.plusDays(1));
        if (finalizada) at.setFechFinReparacionAtenSol(fecha.plusDays(3));
        atencionSolicitudRepository.save(at);

        for (Servicio srv : serviciosRealizados) {
            AtencionServicio as = new AtencionServicio();
            as.setAtencionSolicitud(at);
            as.setServicio(srv);
            as.setFechaAsignacion(fecha.plusDays(1));
            atencionServicioRepository.save(as);
        }

        TecnicoAsignado ta = new TecnicoAsignado();
        ta.setAdministrativo(tecnico);
        ta.setSolicitud(s);
        ta.setFechaAsignacionTecAsig(fecha.plusDays(1));
        tecnicoAsignadoRepository.save(ta);
    }

    /** Mismo formato que el sistema: AAAA + secuencia (ej. 20260001). */
    private Long siguienteNroSolicitud(int gestion) {
        int sec = secuenciaPorGestion.merge(gestion, 1, Integer::sum);
        return Long.parseLong(String.format("%d%0" + digitosSecuencia + "d", gestion, sec));
    }
}

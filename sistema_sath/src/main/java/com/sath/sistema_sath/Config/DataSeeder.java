package com.sath.sistema_sath.Config;

import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.cargo.Cargo;
import com.sath.sistema_sath.domain.cargo.CargoRepository;
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
import com.sath.sistema_sath.domain.unidad.Unidad;
import com.sath.sistema_sath.domain.unidad.UnidadRepository;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import com.sath.sistema_sath.domain.usrRol.UsrRol;
import com.sath.sistema_sath.domain.usrRol.UsrRolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (rolRepository.count() > 0) {
            log.info("DataSeeder omitido — la base de datos ya contiene datos.");
            return;
        }

        log.info("Iniciando carga de datos iniciales...");

        // ── 1. CARGOS ──────────────────────────────────────────────
        Cargo cAdminSistemas = new Cargo();
        cAdminSistemas.setNombreCar("Administrador de Sistemas");
        cargoRepository.save(cAdminSistemas);

        Cargo cTecnicoSoporte = new Cargo();
        cTecnicoSoporte.setNombreCar("Técnico de Soporte");
        cargoRepository.save(cTecnicoSoporte);

        Cargo cJefeUnidad = new Cargo();
        cJefeUnidad.setNombreCar("Jefe de Unidad");
        cargoRepository.save(cJefeUnidad);

        // ── 2. UNIDAD ──────────────────────────────────────────────
        Unidad uTI = new Unidad();
        uTI.setNombreUni("DIRECCIÓN DE TECNOLOGÍAS DE INFORMACIÓN Y COMUNICACIÓN (E)");
        unidadRepository.save(uTI);

        // ── 3. ROLES ───────────────────────────────────────────────
        Rol rolAdmin     = new Rol("ADMINISTRADOR");
        Rol rolTecnico   = new Rol("TECNICO");
        Rol rolUsuario   = new Rol("USUARIO");
        Rol rolSupervisor = new Rol("SUPERVISOR");
        Rol rolDeveloper = new Rol("DEVELOPER");
        rolRepository.saveAll(List.of(rolAdmin, rolTecnico, rolUsuario, rolSupervisor, rolDeveloper));

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
            for (Rol r : List.of(rolAdmin, rolDeveloper)) {
                RolPermiso rp = new RolPermiso();
                rp.setRol(r); rp.setPermiso(p);
                rolPermisoRepository.save(rp);
            }
        }
        // TECNICO → atenciones, solicitudes, historial, reportes, técnicos
        for (Permiso p : List.of(pVerSolicitudes, pAtenderSolicitudes, pVerReportes,
                                  pModAtenciones, pModHistorial, pModReportes, pModTecnicos)) {
            RolPermiso rp = new RolPermiso();
            rp.setRol(rolTecnico); rp.setPermiso(p);
            rolPermisoRepository.save(rp);
        }
        // SUPERVISOR → ver solicitudes y reportes
        for (Permiso p : List.of(pVerSolicitudes, pVerReportes, pModSolicitudes, pModReportes)) {
            RolPermiso rp = new RolPermiso();
            rp.setRol(rolSupervisor); rp.setPermiso(p);
            rolPermisoRepository.save(rp);
        }
        // USUARIO → solo crear y ver solicitudes
        for (Permiso p : List.of(pVerSolicitudes, pGestionarSolicitudes, pModSolicitudes)) {
            RolPermiso rp = new RolPermiso();
            rp.setRol(rolUsuario); rp.setPermiso(p);
            rolPermisoRepository.save(rp);
        }

        // ── 6. PERSONA (appu) ──────────────────────────────────────
        Persona pAppu = new Persona();
        pAppu.setCiPer("12391664");
        pAppu.setNombrePer("Cristopher");
        pAppu.setApPaternoPer("Coaquira");
        pAppu.setApMaternoPer("Guayhua");
        pAppu.setNroCellPer("62487726");
        pAppu.setCorreoPer("coaquira_30361_est@uap.edu.bo");
        personaRepository.save(pAppu);

        // ── 7. USUARIO ─────────────────────────────────────────────
        Usuario uAppu = new Usuario();
        uAppu.setUsername("appu");
        uAppu.setPassword(passwordEncoder.encode("12391664"));
        uAppu.setPersona(pAppu);
        usuarioRepository.save(uAppu);

        // ── 8. USR_ROL ─────────────────────────────────────────────
        for (Rol r : List.of(rolAdmin, rolDeveloper)) {
            UsrRol ur = new UsrRol();
            ur.setUsuario(uAppu); ur.setRol(r);
            usrRolRepository.save(ur);
        }

        // ── 9. ADMINISTRATIVO ──────────────────────────────────────
        Administrativo admAppu = new Administrativo();
        admAppu.setCodigoAdmi(2449);
        admAppu.setPersona(pAppu);
        admAppu.setUnidad(uTI);
        admAppu.setCargo(cAdminSistemas);
        administrativoRepository.save(admAppu);

        // ── 10. PRIORIDADES ────────────────────────────────────────
        Prioridad priAlta = new Prioridad();
        priAlta.setNivel(PrioridadEnum.ALTO);
        priAlta.setOrdenPri(1);
        prioridadRepository.save(priAlta);

        Prioridad priMedia = new Prioridad();
        priMedia.setNivel(PrioridadEnum.MEDIO);
        priMedia.setOrdenPri(2);
        prioridadRepository.save(priMedia);

        Prioridad priBaja = new Prioridad();
        priBaja.setNivel(PrioridadEnum.BAJO);
        priBaja.setOrdenPri(3);
        prioridadRepository.save(priBaja);

        log.info("DataSeeder completado — usuario 'appu' y catálogos base cargados correctamente.");
    }
}
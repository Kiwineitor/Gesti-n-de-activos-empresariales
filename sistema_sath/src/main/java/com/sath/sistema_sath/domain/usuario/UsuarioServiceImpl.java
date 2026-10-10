package com.sath.sistema_sath.domain.usuario;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.cargo.Cargo;
import com.sath.sistema_sath.domain.cargo.CargoRepository;
import com.sath.sistema_sath.domain.persona.Persona;
import com.sath.sistema_sath.domain.persona.PersonaRepository;
import com.sath.sistema_sath.domain.rol.Rol;
import com.sath.sistema_sath.domain.rol.RolRepository;
import com.sath.sistema_sath.domain.unidad.Unidad;
import com.sath.sistema_sath.domain.unidad.UnidadRepository;
import com.sath.sistema_sath.domain.usuario.dto.*;
import com.sath.sistema_sath.domain.usrRol.UsrRol;
import com.sath.sistema_sath.domain.usrRol.UsrRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repo;
    private final UsuarioMapper mapper;
    private final PersonaRepository personaRepo;
    private final RolRepository rolRepo;
    private final UsrRolRepository usrRolRepo;
    private final AdministrativoRepository adminRepo;
    private final CargoRepository cargoRepo;
    private final UnidadRepository unidadRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponseDto create(UsuarioCreateDto dto) {
        Usuario entity = mapper.toEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getIdPersona() != null) {
            Persona persona = personaRepo.findById(dto.getIdPersona())
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + dto.getIdPersona()));
            entity.setPersona(persona);
        }

        return enrichWithRoles(repo.save(entity));
    }

    @Override
    @Transactional
    public UsuarioResponseDto crearCompleto(UsuarioCompletoCreateDto dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("El username ya está en uso: " + dto.getUsername());
        }

        // 1. Crear persona (reusar si ya existe el CI)
        Persona persona = personaRepo.findByCiPer(dto.getCiPer()).orElseGet(() -> {
            Persona nueva = new Persona();
            nueva.setCiPer(dto.getCiPer());
            nueva.setNombrePer(dto.getNombrePer());
            nueva.setApPaternoPer(dto.getApPaternoPer());
            nueva.setApMaternoPer(dto.getApMaternoPer());
            nueva.setNroCellPer(dto.getNroCellPer());
            nueva.setCorreoPer(dto.getCorreoPer());
            return personaRepo.save(nueva);
        });

        // 2. Crear usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setPersona(persona);
        usuario = repo.save(usuario);

        // 3. Asignar rol
        Rol rol = rolRepo.findById(dto.getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + dto.getIdRol()));
        UsrRol usrRol = new UsrRol();
        usrRol.setUsuario(usuario);
        usrRol.setRol(rol);
        usrRolRepo.save(usrRol);

        // 4. Crear administrativo (solo si se proporcionó cargo y unidad)
        if (dto.getIdCargo() != null && dto.getIdUnidad() != null) {
            Cargo cargo = cargoRepo.findById(dto.getIdCargo())
                    .orElseThrow(() -> new IllegalArgumentException("Cargo no encontrado: " + dto.getIdCargo()));
            Unidad unidad = unidadRepo.findById(dto.getIdUnidad())
                    .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada: " + dto.getIdUnidad()));

            Administrativo admin = new Administrativo();
            admin.setPersona(persona);
            admin.setCargo(cargo);
            admin.setUnidad(unidad);
            if (dto.getCodigoAdmi() != null) admin.setCodigoAdmi(dto.getCodigoAdmi());
            adminRepo.save(admin);
        }

        return enrichWithRoles(repo.findById(usuario.getIdUsuario()).orElseThrow());
    }

    @Override
    @Transactional
    public UsuarioResponseDto update(Long id, UsuarioUpdateDto dto) {
        Usuario entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        entity.setUsername(dto.getUsername());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getIdPersona() != null) {
            Persona persona = personaRepo.findById(dto.getIdPersona())
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + dto.getIdPersona()));
            entity.setPersona(persona);
        }

        return enrichWithRoles(repo.save(entity));
    }

    @Override
    @Transactional
    public UsuarioResponseDto changeEstado(Long id, UsuarioChangeEstadoDto dto) {
        if (dto.getEstado() == EstadoEnum.ELIMINADO) {
            throw new IllegalArgumentException("Para eliminar usa el endpoint DELETE.");
        }
        Usuario entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        entity.setEstado(dto.getEstado());
        return enrichWithRoles(repo.save(entity));
    }

    @Override
    @Transactional
    public UsuarioResponseDto toggleEstado(Long id) {
        Usuario entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        entity.setEstado(entity.getEstado() == EstadoEnum.ACTIVO ? EstadoEnum.INACTIVO : EstadoEnum.ACTIVO);
        return enrichWithRoles(repo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> findAll() {
        return repo.findAll().stream()
                .map(this::enrichWithRoles)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto findById(Long id) {
        Usuario entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        return enrichWithRoles(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Usuario entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }

    @Override
    public List<Map<String, Object>> usuariosConAdministrativo() {
        return repo.usuariosConAdministrativo();
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordAdminDto dto) {
        Usuario entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        entity.setPassword(passwordEncoder.encode(dto.getNuevaPassword()));
        repo.save(entity);
    }

    private UsuarioResponseDto enrichWithRoles(Usuario u) {
        UsuarioResponseDto base = mapper.toResponseDto(u);
        List<String> roles = mapper.extractRoles(u.getRoles());
        String cargo = null;
        if (u.getPersona() != null) {
            cargo = adminRepo.findFirstByPersona(u.getPersona())
                    .map(a -> a.getCargo() != null ? a.getCargo().getNombreCar() : null)
                    .orElse(null);
        }
        return new UsuarioResponseDto(
                base.idUsuario(), base.username(), base.idPersona(),
                base.nombrePersona(), base.estado(), roles, cargo, null
        );
    }
}

package com.sath.sistema_sath.domain.tecnicoAsignado;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.Config.Exception.ReglaDeNegocioException;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.administrativo.AdministrativoRepository;
import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.ModoAutoAsignarService;
import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoCreateDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoCreateMasivoDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoMapper;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoResponseDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoUpdateDto;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TecnicoAsignadoServiceImpl implements TecnicoAsignadoService {

    private final AdministrativoRepository adminRepo;
    private final TecnicoAsignadoRepository tecnicoAsignadoRepo;
    private final TecnicoAsignadoMapper mapper;
    private final SolicitudRepository solicitudRepo;
    private final UsuarioRepository usuarioRepo;
    private final ModoAutoAsignarService modoAutoAsignarService;

    @Override
    public TecnicoAsignadoResponseDto create(TecnicoAsignadoCreateDto dto) {
        TecnicoAsignado entity = mapper.toEntity(dto);
        entity.setFechaAsignacionTecAsig(LocalDate.now());
        resolverRelaciones(entity, dto.getIdAdministrativo(), dto.getIdUsuario(), dto.getIdSolicitud());
        return mapper.toResponseDto(tecnicoAsignadoRepo.save(entity));
    }

    @Override
    public List<TecnicoAsignadoResponseDto> createMasivo(TecnicoAsignadoCreateMasivoDto dto) {
        if (dto.getIdsSolicitud() == null || dto.getIdsSolicitud().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una solicitud");
        }
        List<TecnicoAsignado> nuevos = dto.getIdsSolicitud().stream().map(idSolicitud -> {
            TecnicoAsignado entity = new TecnicoAsignado();
            entity.setFechaAsignacionTecAsig(LocalDate.now());
            resolverRelaciones(entity, null, dto.getIdUsuario(), idSolicitud);
            return entity;
        }).toList();
        return mapper.toResponseDtoList(tecnicoAsignadoRepo.saveAll(nuevos));
    }

    @Override
    public List<TecnicoAsignadoResponseDto> findAll() {
        return mapper.toResponseDtoList(tecnicoAsignadoRepo.findAll());
    }

    @Override
    public TecnicoAsignadoResponseDto findById(Long id) {
        return mapper.toResponseDto(tecnicoAsignadoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Técnico asignado no encontrado: " + id)));
    }

    @Override
    public List<Map<String, Object>> listaTecnicoAsignado() {
        return tecnicoAsignadoRepo.listaTecnicoAsignado();
    }

    @Override
    public TecnicoAsignadoResponseDto update(Long id, @Valid @RequestBody TecnicoAsignadoUpdateDto dto) {
        TecnicoAsignado entity = tecnicoAsignadoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Técnico asignado no encontrado: " + id));
        mapper.updateEntityFromDto(dto, entity);
        resolverRelaciones(entity, dto.getIdAdministrativo(), dto.getIdUsuario(), dto.getIdSolicitud());
        return mapper.toResponseDto(tecnicoAsignadoRepo.save(entity));
    }

    @Override
    public void delete(Long id) {
        TecnicoAsignado entity = tecnicoAsignadoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Técnico asignado no encontrado: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        tecnicoAsignadoRepo.save(entity);
    }

    @Override
    @jakarta.transaction.Transactional
    public TecnicoAsignadoResponseDto tomar(String username, Long idSolicitud) {
        if (!modoAutoAsignarService.isActivo())
            throw new ReglaDeNegocioException("El modo auto-asignación no está activo.");

        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new ReglaDeNegocioException("Usuario no encontrado: " + username));
        if (usuario.getPersona() == null)
            throw new ReglaDeNegocioException("El usuario no tiene persona vinculada.");

        Administrativo admin = adminRepo.findFirstByPersona(usuario.getPersona())
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "El usuario \"" + username + "\" no tiene perfil administrativo."));

        var solicitud = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new ReglaDeNegocioException("Solicitud no encontrada: " + idSolicitud));

        if (solicitud.getEstadoSol() != EstadoSolicitudEnum.PENDIENTE)
            throw new ReglaDeNegocioException(
                    "La solicitud #" + solicitud.getNroSol() + " ya no está pendiente.");

        if (tecnicoAsignadoRepo.existsByAdministrativoAndSolicitud(admin, solicitud))
            throw new ReglaDeNegocioException(
                    "Ya estás asignado a la solicitud #" + solicitud.getNroSol() + ".");

        TecnicoAsignado asignacion = new TecnicoAsignado();
        asignacion.setAdministrativo(admin);
        asignacion.setSolicitud(solicitud);
        asignacion.setFechaAsignacionTecAsig(LocalDate.now());
        return mapper.toResponseDto(tecnicoAsignadoRepo.save(asignacion));
    }

    private void resolverRelaciones(TecnicoAsignado entity, Long idAdministrativo, Long idUsuario, Long idSolicitud) {
        if (idAdministrativo != null) {
            entity.setAdministrativo(adminRepo.findById(idAdministrativo)
                    .orElseThrow(() -> new IllegalArgumentException("Administrativo no encontrado: " + idAdministrativo)));
        } else if (idUsuario != null) {
            Usuario usuario = usuarioRepo.findById(idUsuario)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));
            if (usuario.getPersona() == null)
                throw new IllegalArgumentException("El usuario seleccionado no tiene persona vinculada");
            Administrativo admin = adminRepo.findFirstByPersona(usuario.getPersona())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El usuario \"" + usuario.getUsername() + "\" no tiene perfil de administrativo."));
            entity.setAdministrativo(admin);
        }

        if (idSolicitud != null) {
            entity.setSolicitud(solicitudRepo.findById(idSolicitud)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud)));
        }
    }
}

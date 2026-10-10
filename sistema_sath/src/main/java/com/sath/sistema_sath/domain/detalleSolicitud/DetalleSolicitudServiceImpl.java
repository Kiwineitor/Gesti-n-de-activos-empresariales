package com.sath.sistema_sath.domain.detalleSolicitud;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.activo.ActivoRepository;
import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudCreateDto;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudResponseDto;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudUpdateDto;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolucionMapper;
import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DetalleSolicitudServiceImpl implements DetalleSolicitudService {

    private final DetalleSolicitudRepository repo;
    private final DetalleSolucionMapper mapper;
    private final ActivoRepository activoRepo;
    private final SolicitudRepository solicitudRepo;

    @Override
    public DetalleSolicitudResponseDto create(DetalleSolicitudCreateDto dto) {
        DetalleSolicitud entity = mapper.toEntity(dto);
        resolverRelaciones(entity, dto.getIdSolicitud(), dto.getIdActivo());
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public List<DetalleSolicitudResponseDto> findAll() {
        return mapper.toResponseDtoList(repo.findAll());
    }

    @Override
    public DetalleSolicitudResponseDto findById(Long id) {
        DetalleSolicitud entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("DetalleSolicitud no encontrado"));
        return mapper.toResponseDto(entity);
    }

    @Override
    public DetalleSolicitudResponseDto update(Long id, DetalleSolicitudUpdateDto dto) {
        DetalleSolicitud entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("DetalleSolicitud no encontrado"));
        mapper.updateEntityFromDto(dto, entity);
        resolverRelaciones(entity, dto.getIdSolicitud(), dto.getIdActivo());
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public void delete(Long id) {
        DetalleSolicitud entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("DetalleSolicitud no encontrado"));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }

    @Override
    public void tipoInforme(Long id,  TipoInformeEnum dto) {
        DetalleSolicitud entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("DetalleSolicitud no encontrado"));
        entity.setTipoInforme(dto);
        repo.save(entity);
    }

    @Override
    public List<Map<String, Object>> listaParaELFrontend() {
        return repo.listaParaELFrontend();
    }

    private void resolverRelaciones(DetalleSolicitud entity, Long idSolicitud, Long idActivo) {
        if (idSolicitud != null) {
            entity.setSolicitud(solicitudRepo.findById(idSolicitud).orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada")));
        }
        if (idActivo != null) {
            entity.setActivo(activoRepo.findById(idActivo).orElseThrow(() -> new IllegalArgumentException("Activo no encontrado" )));
        }
    }
}

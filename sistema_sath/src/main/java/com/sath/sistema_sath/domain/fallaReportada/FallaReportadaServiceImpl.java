package com.sath.sistema_sath.domain.fallaReportada;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.tools.colas.ColaPriorizadaSolicitudes;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitudRepository;
import com.sath.sistema_sath.domain.falla.Falla;
import com.sath.sistema_sath.domain.falla.FallaRepository;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaCreateDto;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaMapper;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaResponseDto;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaUpdateDto;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FallaReportadaServiceImpl implements FallaReportadaService {

    private final FallaReportadaRepository repo;
    private final FallaReportadaMapper mapper;
    private final FallaRepository fallaRepository;
    private final DetalleSolicitudRepository detalleSolicitudRepository;
    private final ColaPriorizadaSolicitudes colaPriorizada;

    @Override
    public FallaReportadaResponseDto create(FallaReportadaCreateDto dto) {
        FallaReportada entity = mapper.toEntity(dto);
        resolverRelaciones(entity, dto.getIdFalla(), dto.getIdDetalleSolicitud());
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public List<FallaReportadaResponseDto> findAll() {
        return mapper.toResponseDtoList(repo.findAll());
    }

    @Override
    public FallaReportadaResponseDto findById(Long idFallaReportada) {
        FallaReportada entity = repo.findById(idFallaReportada)
                .orElseThrow(() -> new IllegalArgumentException("Falla reportada no encontrada con ID: " + idFallaReportada));
        return mapper.toResponseDto(entity);
    }

    @Override
    public FallaReportadaResponseDto update(Long idFallaReportada, FallaReportadaUpdateDto dto) {
        FallaReportada entity = repo.findById(idFallaReportada)
                .orElseThrow(() -> new IllegalArgumentException("Falla reportada no encontrada con ID: " + idFallaReportada));
        mapper.updateEntityFromDto(dto, entity);
        resolverRelaciones(entity, dto.getIdFalla(), dto.getIdDetalleSolicitud());
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public void delete(Long idFallaReportada) {
        FallaReportada entity = repo.findById(idFallaReportada)
                .orElseThrow(() -> new IllegalArgumentException("Falla reportada no encontrada con ID: " + idFallaReportada));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }

    @Override
    public List<Map<String, Object>> listaFallaReportada() {
        return repo.listaFallaReportada();
    }

    /**
     * El técnico asigna una Falla del catálogo a una FallaReportada sin categorizar.
     */
    @Override
    @Transactional
    public FallaReportadaResponseDto catalogarFalla(Long idFallaReportada, Long idFalla) {
        FallaReportada fallaReportada = repo.findById(idFallaReportada)
                .orElseThrow(() -> new IllegalArgumentException("FallaReportada no encontrada con ID: " + idFallaReportada));

        Falla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new IllegalArgumentException("Falla no encontrada con ID: " + idFalla));

        fallaReportada.setFalla(falla);
        FallaReportada saved = repo.save(fallaReportada);
        log.info("FallaReportada {} catalogada como '{}'", idFallaReportada, falla.getNombreFall());

        return mapper.toResponseDto(saved);
    }

    private void resolverRelaciones(FallaReportada entity, Long idFalla, Long idDetalleSolicitud) {
        if (idFalla != null) {
            entity.setFalla(fallaRepository.findById(idFalla)
                    .orElseThrow(() -> new IllegalArgumentException("Falla no encontrada con ID: " + idFalla)));
        }
        if (idDetalleSolicitud != null) {
            entity.setDetalleSolicitud(detalleSolicitudRepository.findById(idDetalleSolicitud)
                    .orElseThrow(() -> new IllegalArgumentException("DetalleSolicitud no encontrada con ID: " + idDetalleSolicitud)));
        }
    }
}

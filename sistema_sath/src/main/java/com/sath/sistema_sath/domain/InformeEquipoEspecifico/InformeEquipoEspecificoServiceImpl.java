package com.sath.sistema_sath.domain.InformeEquipoEspecifico;

import com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto.InformeEquipoEspecificoCreateDto;
import com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto.InformeEquipoEspecificoMapper;
import com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto.InformeEquipoEspecificoResponseDto;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitudRepository;
import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InformeEquipoEspecificoServiceImpl implements InformeEquipoEspecificoService {

    private final InformeEquipoEspecificoRepository repo;
    private final InformeEquipoEspecificoMapper mapper;
    private final DetalleSolicitudRepository detalleRepo;


    @Override
    @Transactional
    public InformeEquipoEspecificoResponseDto create(InformeEquipoEspecificoCreateDto dto) {
        var detalle = detalleRepo.findById(dto.getIdDetalleSolicitud())
                .orElseThrow(() -> new IllegalArgumentException("DetalleSolicitud no encontrado: " + dto.getIdDetalleSolicitud()));

        if (repo.existsByDetalleSolicitudIdDetalleSolicitud(dto.getIdDetalleSolicitud())) {
            throw new IllegalStateException("Ya existe un informe específico para este recurso");
        }

        InformeEquipoEspecifico entity = mapper.toEntity(dto);
        entity.setDetalleSolicitud(detalle);
        entity.setNroInforme(generarSiguienteNroInforme());
        entity.setFechaCierreInformeEsp(LocalDate.now());
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public InformeEquipoEspecificoResponseDto findById(Long id) {
        InformeEquipoEspecifico entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("InformeEquipoEspecifico no encontrado con ID: " + id));

        return mapper.toResponseDto(entity);
    }

    @Override
    public List<InformeEquipoEspecificoResponseDto> findAll() {
        return mapper.toResponseDtoList(repo.findAll());
    }

    @Override
    public List<Map<String, Object>> listaInformeEquipoEspecifico() {
        return repo.listaInformeEquipoEspecifico();
    }

    private int generarSiguienteNroInforme() {
        return repo.findMaxNroInforme() + 1;
    }
}

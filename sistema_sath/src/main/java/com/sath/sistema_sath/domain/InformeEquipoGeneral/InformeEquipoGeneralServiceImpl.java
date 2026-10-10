package com.sath.sistema_sath.domain.InformeEquipoGeneral;

import com.sath.sistema_sath.Config.Exception.ReglaDeNegocioException;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.dto.InformeEquipoGeneralCreateDto;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.dto.InformeEquipoGeneralMapper;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.dto.InformeEquipoGeneralResponseDto;
import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InformeEquipoGeneralServiceImpl implements  InformeEquipoGeneralService{

    private final InformeEquipoGeneralRepository repo;
    private final InformeEquipoGeneralMapper mapper;
    private final SolicitudRepository solicitudRepo;


    @Override
    @Transactional
    public InformeEquipoGeneralResponseDto create(InformeEquipoGeneralCreateDto dto) {
        // Verificar que la solicitud existe y está FINALIZADA
        Solicitud solicitud = solicitudRepo.findById(dto.getIdSolicitud())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (solicitud.getEstadoSol() != EstadoSolicitudEnum.FINALIZADO) {
            throw new IllegalStateException("Solo se puede crear informe general de solicitudes FINALIZADAS");
        }

        // Verificar que no tenga ya un informe general
        if (repo.existsBySolicitudIdSolicitud(dto.getIdSolicitud())) {
            throw new ReglaDeNegocioException("Esta solicitud ya tiene un informe general");
        }

        InformeEquipoGeneral entity = mapper.toEntity(dto);
        entity.setSolicitud(solicitud);
        entity.setNroInforme(generarSiguienteNroInforme());
        entity.setFechaCierreInformeGnr(LocalDate.now());

        try {
            return mapper.toResponseDto(repo.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException("Esta solicitud ya tiene un informe general");
        }
    }

    @Override
    public InformeEquipoGeneralResponseDto findById(Long id) {
        InformeEquipoGeneral entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("InformeEquipoGeneral no encontrado con ID: " + id));
        return mapper.toResponseDto(entity);
    }

    @Override
    public List<InformeEquipoGeneralResponseDto> findAll() {
        return mapper.toResponseDtoList(repo.findAll());
    }

    @Override
    public List<Map<String, Object>> listaInformeEquipoGeneral() {
        return repo.listaInformeEquipoGeneral();
    }

    private int generarSiguienteNroInforme() {
        return repo.findMaxNroInforme() + 1;
    }
}

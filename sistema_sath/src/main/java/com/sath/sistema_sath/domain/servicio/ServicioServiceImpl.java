package com.sath.sistema_sath.domain.servicio;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.servicio.dto.ServicioCreateDto;
import com.sath.sistema_sath.domain.servicio.dto.ServicioMapper;
import com.sath.sistema_sath.domain.servicio.dto.ServicioResponseDto;
import com.sath.sistema_sath.domain.tipoServicio.TipoServicio;
import com.sath.sistema_sath.domain.tipoServicio.TipoServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository repo;
    private final ServicioMapper mapper;
    private final TipoServicioRepository tipoServicioRepo;

    @Override
    public ServicioResponseDto create(ServicioCreateDto dto) {
        Servicio entity = mapper.toEntity(dto);
        if (dto.getIdTipoServicio() != null) {
            TipoServicio tipo = tipoServicioRepo.findById(dto.getIdTipoServicio())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de servicio no encontrado: " + dto.getIdTipoServicio()));
            entity.setTipoServicio(tipo);
        }
        return mapper.toResponseDto(repo.save(entity));
    }



    @Override
    public ServicioResponseDto findById(Long id) {
        Servicio entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        return mapper.toResponseDto(entity);
    }

    @Override
    public List<Map<String, Object>> listaServicio() {
        return repo.listaServicio();
    }

    @Override
    public ServicioResponseDto update(Long id, ServicioCreateDto dto) {
        Servicio entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        entity.setNombreServ(dto.getNombreServ());
        entity.setObservacionesServ(dto.getObservacionesServ());
        if (dto.getIdTipoServicio() != null) {
            TipoServicio tipo = tipoServicioRepo.findById(dto.getIdTipoServicio())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de servicio no encontrado: " + dto.getIdTipoServicio()));
            entity.setTipoServicio(tipo);
        } else {
            entity.setTipoServicio(null);
        }
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public void delete(Long id) {
        Servicio entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }
}

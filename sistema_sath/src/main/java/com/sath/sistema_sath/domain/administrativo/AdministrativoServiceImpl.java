package com.sath.sistema_sath.domain.administrativo;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoCreateDto;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoMapper;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoResponseDto;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoUpdateDto;
import com.sath.sistema_sath.domain.cargo.CargoRepository;
import com.sath.sistema_sath.domain.persona.PersonaRepository;
import com.sath.sistema_sath.domain.unidad.UnidadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdministrativoServiceImpl implements AdministrativoService {

    private final AdministrativoRepository adminRepo;
    private final PersonaRepository personaRepo;
    private final UnidadRepository unidadRepo;
    private final CargoRepository cargoRepo;
    private final AdministrativoMapper mapper;


    @Override
    @Transactional
    public AdministrativoResponseDto create(AdministrativoCreateDto dto) {
        Administrativo entity = mapper.toEntity(dto);
        resolverRelaciones(entity, dto.getIdPersona(), dto.getIdCargo(), dto.getIdUnidad());
        return mapper.toResponseDto(adminRepo.save(entity));
    }

    @Override
    @Transactional
    public List<AdministrativoResponseDto> findAll() {
        return mapper.toResponseDtoList(adminRepo.findAll());
    }

    @Override
    @Transactional
    public AdministrativoResponseDto findById(Long id) {
        Administrativo entity = adminRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrativo no encontrado con ID: " + id));

        return mapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public AdministrativoResponseDto update(Long id,  AdministrativoUpdateDto dto) {
        Administrativo entity = adminRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrativo no encontrado con ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        resolverRelaciones(entity, dto.getIdPersona(), dto.getIdCargo(), dto.getIdUnidad());
        return mapper.toResponseDto(adminRepo.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Administrativo entity = adminRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrativo no encontrado con ID: " + id));

        entity.setEstado(EstadoEnum.ELIMINADO);
        adminRepo.save(entity);

    }

    @Override
    public List<Map<String, Object>> listaParaElFrontend() {
        return adminRepo. listaParaElFrontend();
    }

    //-----------Metodos auxiliares----------------
    private void resolverRelaciones(Administrativo entity, Long personaId, Long cargoId, Long unidadId) {

        if (personaId != null) {
            entity.setPersona(personaRepo.findById(personaId)
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + personaId)));
        }
        if (cargoId != null) {
            entity.setCargo(cargoRepo.findById(cargoId)
                    .orElseThrow(() -> new IllegalArgumentException("Cargo no encontrado con ID: " + cargoId)));
        }
        if (unidadId != null) {
            entity.setUnidad(unidadRepo.findById(unidadId)
                    .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada con ID: " + unidadId)));
        }

    }

}
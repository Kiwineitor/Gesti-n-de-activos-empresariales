package com.sath.sistema_sath.domain.cargo;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.cargo.dto.CargoCreateDto;
import com.sath.sistema_sath.domain.cargo.dto.CargoMapper;
import com.sath.sistema_sath.domain.cargo.dto.CargoResponseDto;
import com.sath.sistema_sath.domain.cargo.interfaz.CargoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CargoServiceImpl implements CargoService {

    private final CargoRepository cargoRepository;
    private final CargoMapper mapper;


    @Override
    @CacheEvict(value = "cargo", allEntries = true)
    public CargoResponseDto create(CargoCreateDto Dto) {
        Cargo entity = mapper.toEntity(Dto);
        return mapper.toResponseDto(cargoRepository.save(entity));
    }

    @Override
    public List<CargoResponseDto> findAll() {
        return mapper.toResponseDtoList(cargoRepository.findAll());
    }

    @Override
    public CargoResponseDto findById(Long id) {
        Cargo entity = cargoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cargo no encontrado con ID: " + id));
        return mapper.toResponseDto(entity);
    }

    @Override
    @CacheEvict(value = "cargo", allEntries = true)
    public void delete(Long id) {
        Cargo entity = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        cargoRepository.save(entity);
    }

    @Override
    @Cacheable("cargo")
    public List<Map<String, Object>> listaParaElFrontend() {
        return cargoRepository.listaParaElFrontend();
    }

    @Override
    public List<CargoProjection> findByEstado(String estado) {
        return cargoRepository.findByEstado(estado);
    }

}

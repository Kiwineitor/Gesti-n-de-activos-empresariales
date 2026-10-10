package com.sath.sistema_sath.domain.cargo;

import com.sath.sistema_sath.domain.cargo.interfaz.CargoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface CargoRepository extends JpaRepository<Cargo, Long> {

    @Query(value = "select c.nombre_car as mombreCar from cargo c where c._estado_aud = :estado", nativeQuery = true)
    List<CargoProjection> findByEstado(@Param("estado") String estado);

    @Query(value="SELECT c.nombre_car as nombreCar FROM cargo c WHERE c._estado_aud = 'ACTIVO'", nativeQuery = true)
    List<CargoProjection> findAllActivos();

    boolean existsByNombreCar(String nombreCar);

    @Query(value = "select c.id_cargo as idCar, c.nombre_car as nombreCar from cargo c", nativeQuery = true)
    List<Map<String, Object>> listaParaElFrontend();

    Optional<Cargo> findByNombreCar(String nombre);
}

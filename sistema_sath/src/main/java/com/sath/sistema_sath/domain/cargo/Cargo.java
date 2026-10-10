package com.sath.sistema_sath.domain.cargo;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "cargo")
public class Cargo extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCargo;

    @Column(name = "nombre_car", length = 100)
    private String nombreCar;

    @OneToMany(mappedBy = "cargo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Administrativo> administrativos;


}

package com.sath.sistema_sath.domain.unidad;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "unidad")
public class Unidad extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUnidad;

    @Column(name = "nombre_uni", length = 100, unique = true)
    private String nombreUni;




    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Administrativo> administrativo;

}

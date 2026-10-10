package com.sath.sistema_sath.domain.tipoServicio;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.servicio.Servicio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tipo_servicio")
public class TipoServicio extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoServicio;

    @Column(name = "nombre_tipo", length = 100, nullable = false, unique = true)
    private String nombreTipo;

    @Column(name = "descripcion_tipo", length = 300)
    private String descripcionTipo;

    @OneToMany(mappedBy = "tipoServicio", fetch = FetchType.LAZY)
    private List<Servicio> servicios;
}

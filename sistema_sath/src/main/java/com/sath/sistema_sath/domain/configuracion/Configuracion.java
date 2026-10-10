package com.sath.sistema_sath.domain.configuracion;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.unidad.Unidad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "configuracion",
        uniqueConstraints = @UniqueConstraint(name = "uq_configuracion_unidad_clave",
                columnNames = {"id_unidad", "clave"}))
public class Configuracion extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConfiguracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad")
    private Unidad unidad;

    @Column(name = "clave", length = 100, nullable = false)
    private String clave;

    @Column(name = "valor", length = 500)
    private String valor;
}

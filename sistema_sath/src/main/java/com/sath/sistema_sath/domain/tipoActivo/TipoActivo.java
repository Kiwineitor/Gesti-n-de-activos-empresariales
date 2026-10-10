package com.sath.sistema_sath.domain.tipoActivo;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.activo.Activo;
import com.sath.sistema_sath.domain.falla.Falla;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tipo_activo")
public class TipoActivo extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoActivo;

    @Column(name = "nombre_tipo_act", length = 100)
    private String nombreTipoAct;

    // Cambiado a LAZY para evitar traer toda la BD de golpe
    @OneToMany(mappedBy = "tipoActivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Activo> activos;

    @OneToMany(mappedBy = "tipoActivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Falla> fallas;
}

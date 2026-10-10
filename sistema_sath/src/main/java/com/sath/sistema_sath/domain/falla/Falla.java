package com.sath.sistema_sath.domain.falla;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportada;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "falla")
public class Falla extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFalla;

    @Column(name = "nombre_fall", length = 100)
    private String nombreFall;


    @ManyToOne
    @JoinColumn(name = "id_tipo_activo")
    private TipoActivo tipoActivo;

    @OneToMany(mappedBy = "falla", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FallaReportada> fallasReportadas;
}

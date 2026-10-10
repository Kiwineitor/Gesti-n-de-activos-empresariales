package com.sath.sistema_sath.domain.evidenciaFoto;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "evidencia_foto")
@Getter
@Setter
public class EvidenciaFoto extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvidenciaFoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    @Column(name = "nombre_original")
    private String nombreOriginal;

    @Column(name = "descripcion", length = 300)
    private String descripcion;
}
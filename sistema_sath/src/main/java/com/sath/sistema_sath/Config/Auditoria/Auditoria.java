package com.sath.sistema_sath.Config.Auditoria;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditoria {

    @CreatedDate
    @Column(name = "_fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @CreatedBy
    @Column(name = "_registrado_por_id_usuario", updatable = false)
    private Long registradoPorIdUsuario;

    @LastModifiedDate
    @Column(name = "_fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @LastModifiedBy
    @Column(name = "_modificado_por_id_usuario")
    private Long modificadoPorIdUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "_estado_aud", nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'ACTIVO'")
    private EstadoEnum estado = EstadoEnum.ACTIVO;

    @PrePersist
    protected void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoEnum.ACTIVO;
        }
    }
}
package com.sath.sistema_sath.domain.notificacion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 512)
    private String descripcion;

    @Column(nullable = false)
    private boolean leida = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    protected void prePersist() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}
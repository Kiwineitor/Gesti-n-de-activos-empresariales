package com.sath.sistema_sath.domain.historialEstado;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "historial_estado")
@Entity
@Getter
@Setter
public class HistorialEstado extends Auditoria{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorialEstado;
    
}

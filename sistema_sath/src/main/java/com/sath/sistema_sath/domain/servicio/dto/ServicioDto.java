package com.sath.sistema_sath.domain.servicio.dto;

import lombok.Value;

 
/**
 * DTO de solo lectura — para consultas internas entre módulos.
 * @Value hace todos los campos final e inmutables — correcto para un DTO de lectura.
 * NO uses @Setter con @Value — son incompatibles.
 */
@Value
public class ServicioDto {
    Long   idServicio;
    String nombreServ;
}
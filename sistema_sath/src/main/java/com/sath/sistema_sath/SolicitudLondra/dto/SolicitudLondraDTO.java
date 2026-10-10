package com.sath.sistema_sath.SolicitudLondra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudLondraDTO {

    @JsonProperty("idSolicitud")
    private Integer idSolicitudLondra;

    @JsonProperty("numeroSolicitud")
    private String numeroSolicitudLondra;

    private String estadoSolicitud;
    private String fechaSolicitud;
    private String descripcion;

    private FuncionarioLondraDTO funcionario;
    private List<DetalleActivoLondraDTO> equipos;

    @Data
    public static class FuncionarioLondraDTO {
        private String nombre;
        private String paterno;
        private String materno;
        private String ci;
        private String celular;
        private String correo;
        private Integer codigoAdministrativo;
        private String nombreUnidad;
        private String cargoSolicitante;
    }

    @Data
    public static class DetalleActivoLondraDTO {
        private Long idDetalle;
        private String codigoActivo;
        private String descripcionActivo;
        private String tipoActivo;
        private String problemaEspecifico;
    }
}

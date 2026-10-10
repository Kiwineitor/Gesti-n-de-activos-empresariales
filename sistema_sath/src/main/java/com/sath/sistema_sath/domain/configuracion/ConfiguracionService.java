package com.sath.sistema_sath.domain.configuracion;

import com.sath.sistema_sath.domain.configuracion.dto.ConfiguracionReporteDto;

public interface ConfiguracionService {

    String obtenerValor(String clave, String valorPorDefecto);

    ConfiguracionReporteDto obtenerConfiguracionReportes();

    ConfiguracionReporteDto guardarValores(String nombreAutorizado, String cargoAutorizado);
}

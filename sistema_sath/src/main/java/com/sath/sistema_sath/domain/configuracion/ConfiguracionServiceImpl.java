package com.sath.sistema_sath.domain.configuracion;

import com.sath.sistema_sath.domain.configuracion.dto.ConfiguracionReporteDto;
import com.sath.sistema_sath.domain.unidad.Unidad;
import com.sath.sistema_sath.domain.unidad.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracionServiceImpl implements ConfiguracionService {

    public static final String CLAVE_NOMBRE_AUTORIZADO = "reporte.autorizado.nombre";
    public static final String CLAVE_CARGO_AUTORIZADO  = "reporte.autorizado.cargo";

    public static final String NOMBRE_AUTORIZADO_POR_DEFECTO = "Lic. Eduardo Zubieta Copeticon";
    public static final String CARGO_AUTORIZADO_POR_DEFECTO  = "JEFE a.i DIRECCION DE TECNOLOGIA, INFORMACION Y COMUNICACION";

    private final ConfiguracionRepository configuracionRepository;
    private final UnidadRepository unidadRepository;

    @Override
    public String obtenerValor(String clave, String valorPorDefecto) {
        Configuracion config = configuracionRepository
                .findByUnidad_IdUnidadAndClave(obtenerUnidadDtic().getIdUnidad(), clave)
                .orElse(null);
        return (config == null || config.getValor() == null || config.getValor().isBlank())
                ? valorPorDefecto
                : config.getValor();
    }

    @Override
    public ConfiguracionReporteDto obtenerConfiguracionReportes() {
        return new ConfiguracionReporteDto(
                obtenerValor(CLAVE_NOMBRE_AUTORIZADO, NOMBRE_AUTORIZADO_POR_DEFECTO),
                obtenerValor(CLAVE_CARGO_AUTORIZADO, CARGO_AUTORIZADO_POR_DEFECTO));
    }

    @Override
    @Transactional
    public ConfiguracionReporteDto guardarValores(String nombreAutorizado, String cargoAutorizado) {
        Unidad dtic = obtenerUnidadDtic();
        guardar(dtic, CLAVE_NOMBRE_AUTORIZADO, nombreAutorizado);
        guardar(dtic, CLAVE_CARGO_AUTORIZADO, cargoAutorizado);
        return obtenerConfiguracionReportes();
    }

    private void guardar(Unidad dtic, String clave, String valor) {
        Configuracion config = configuracionRepository
                .findByUnidad_IdUnidadAndClave(dtic.getIdUnidad(), clave)
                .orElseGet(() -> {
                    Configuracion nueva = new Configuracion();
                    nueva.setUnidad(dtic);
                    nueva.setClave(clave);
                    return nueva;
                });
        config.setValor(valor);
        configuracionRepository.save(config);
    }

    private Unidad obtenerUnidadDtic() {
        return unidadRepository.findFirstByNombreUniContainsIgnoreCaseAndNombreUniContainsIgnoreCase("TECNOLOG", "INFORMACI")
                .orElseGet(() -> unidadRepository.findFirstByNombreUniContainsIgnoreCase("TECNOLOG")
                        .orElseThrow(() -> new IllegalStateException("No se encontró la unidad DTIC para la configuración de reportes")));
    }
}

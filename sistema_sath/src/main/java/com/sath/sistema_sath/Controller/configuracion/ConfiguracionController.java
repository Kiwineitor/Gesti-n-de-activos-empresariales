package com.sath.sistema_sath.Controller.configuracion;

import com.sath.sistema_sath.domain.configuracion.ConfiguracionService;
import com.sath.sistema_sath.domain.configuracion.dto.ConfiguracionReporteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService service;

    @GetMapping("/dtic")
    @ResponseBody
    public ResponseEntity<ConfiguracionReporteDto> obtenerConfiguracion() {
        return ResponseEntity.ok(service.obtenerConfiguracionReportes());
    }

    @PutMapping("/dtic")
    @ResponseBody
    public ResponseEntity<ConfiguracionReporteDto> guardarConfiguracion(@RequestBody ConfiguracionReporteDto dto) {
        return ResponseEntity.ok(service.guardarValores(dto.nombreAutorizado(), dto.cargoAutorizado()));
    }
}

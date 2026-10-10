package com.sath.sistema_sath.Controller.reportes;

import com.sath.sistema_sath.tools.reportes.ReporteGenericoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteGenericoService reporteService;

//    @GetMapping("/detalle-solicitud/{nroSol}") -- Api para generar reportes en el front para un framework
    public ResponseEntity<?> getDetalleSolicitud(@PathVariable String nroSol) {
        List<Map<String, Object>> datos = reporteService.obtenerDetalleSolicitud(nroSol);
        if (datos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(datos);
    }
}

package com.sath.sistema_sath.Controller.publico;

import com.sath.sistema_sath.domain.activo.Activo;
import com.sath.sistema_sath.domain.activo.ActivoRepository;
import com.sath.sistema_sath.domain.cargo.Cargo;
import com.sath.sistema_sath.domain.cargo.CargoRepository;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivoRepository;
import com.sath.sistema_sath.domain.unidad.Unidad;
import com.sath.sistema_sath.domain.unidad.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCatalogoController {

    private final UnidadRepository unidadRepo;
    private final CargoRepository cargoRepo;
    private final TipoActivoRepository tipoActivoRepo;
    private final ActivoRepository activoRepo;

    @GetMapping("/unidades")
    public List<Map<String, Object>> unidades() {
        return unidadRepo.listaUnidad();
    }

    @PostMapping("/unidades")
    public ResponseEntity<?> crearUnidad(@RequestBody Map<String, String> body) {
        String nombre = body.getOrDefault("nombre", "").trim();
        if (nombre.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El nombre es obligatorio"));
        }
        if (unidadRepo.findByNombreUni(nombre).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ya existe una unidad con ese nombre"));
        }
        Unidad unidad = new Unidad();
        unidad.setNombreUni(nombre);
        Unidad guardada = unidadRepo.save(unidad);
        return ResponseEntity.ok(Map.of(
                "idUnidad", guardada.getIdUnidad(),
                "nombreUni", guardada.getNombreUni()
        ));
    }

    @GetMapping("/cargos")
    public List<Map<String, Object>> cargos() {
        return cargoRepo.listaParaElFrontend();
    }

    @PostMapping("/cargos")
    public ResponseEntity<?> crearCargo(@RequestBody Map<String, String> body) {
        String nombre = body.getOrDefault("nombre", "").trim();
        if (nombre.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El nombre es obligatorio"));
        }
        if (cargoRepo.findByNombreCar(nombre).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ya existe un cargo con ese nombre"));
        }
        Cargo cargo = new Cargo();
        cargo.setNombreCar(nombre);
        Cargo guardado = cargoRepo.save(cargo);
        return ResponseEntity.ok(Map.of(
                "idcar", guardado.getIdCargo(),
                "nombrecar", guardado.getNombreCar()
        ));
    }

    @GetMapping("/tipos-activo")
    public List<Map<String, Object>> tiposActivo() {
        return tipoActivoRepo.listaTipoActivo();
    }

    @GetMapping("/activos/buscar")
    public ResponseEntity<?> buscarActivo(@RequestParam String codigo) {
        return activoRepo.findByCodigoAct(codigo.trim())
                .<ResponseEntity<?>>map(a -> ResponseEntity.ok(Map.of(
                        "idActivo", a.getIdActivo(),
                        "codigoActivo", a.getCodigoAct(),
                        "descripcionActivo", a.getDescripcionAct() != null ? a.getDescripcionAct() : "",
                        "tipoActivo", a.getTipoActivo() != null ? a.getTipoActivo().getNombreTipoAct() : ""
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("mensaje", "No se encontró recurso con código: " + codigo.trim())));
    }
}

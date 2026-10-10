package com.sath.sistema_sath.Controller.detalleSolicitud;

import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitudService;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudTipoInformeDto;
import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudCreateDto;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudResponseDto;
import com.sath.sistema_sath.domain.detalleSolicitud.dto.DetalleSolicitudUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/detalleSolicitud")
public class DetalleSolicitudController {

    private final DetalleSolicitudService service;


    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<DetalleSolicitudResponseDto> create(@Valid @RequestBody DetalleSolicitudCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<DetalleSolicitudResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<DetalleSolicitudResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<DetalleSolicitudResponseDto> update(@PathVariable Long id, @Valid @RequestBody DetalleSolicitudUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tiposInforme")
    @ResponseBody
    public ResponseEntity<TipoInformeEnum[]> listarTiposInforme() {
        return ResponseEntity.ok(TipoInformeEnum.values());
    }

    @PatchMapping("/{id}/tipoInforme")
    @ResponseBody
    public ResponseEntity<String> tipoInforme(
            @PathVariable Long id,
            @RequestBody DetalleSolicitudTipoInformeDto dto
    ) {
        service.tipoInforme(id, dto.tipoInforme());
        return ResponseEntity.ok("Tipo de informe seleccionado correctamente");
    }

    @GetMapping("/listaParaElFrontend")
    @ResponseBody
    public ResponseEntity<?> listaParaElFrontend() {
        try {
            List<Map<String, Object>> lista = service.listaParaELFrontend();

            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al recuperar la lista de activos");
            error.put("mensaje", e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // 500 Error

        }
    }

}

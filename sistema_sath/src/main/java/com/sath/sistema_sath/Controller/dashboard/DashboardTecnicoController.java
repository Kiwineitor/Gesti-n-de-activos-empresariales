package com.sath.sistema_sath.Controller.dashboard;

import com.sath.sistema_sath.tools.reportes.ReporteGenericoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardTecnicoController {

    private final ReporteGenericoRepository reporteRepo;


    @GetMapping("/mis-stats")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO','DEVELOPER')")
    public ResponseEntity<?> misStats(Principal principal) {
        try {
            Map<String, Object> resp = buildStats(principal.getName());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/tecnico/{username}/stats")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DEVELOPER')")
    public ResponseEntity<?> statsPorTecnico(@PathVariable String username) {
        try {
            Map<String, Object> resp = buildStats(username);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/lista-tecnicos")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DEVELOPER')")
    public ResponseEntity<?> listaTecnicos() {
        try {
            List<Map<String, Object>> lista = reporteRepo.listaTecnicosConStats();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Map<String, Object> buildStats(String username) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("kpis", reporteRepo.statsTecnico(username));
        resp.put("porMes", reporteRepo.solicitudesPorMesTecnico(username));
        resp.put("topFallas", reporteRepo.topFallasTecnico(username));
        return resp;
    }

    private ResponseEntity<Map<String, Object>> errorResponse(Exception e) {
        Map<String, Object> err = new HashMap<>();
        err.put("error", e.getMessage());
        return ResponseEntity.internalServerError().body(err);
    }
}

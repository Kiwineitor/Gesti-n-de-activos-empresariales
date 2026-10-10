package com.sath.sistema_sath.Controller.reportes;

import com.sath.sistema_sath.tools.jasper.ReporteSecundarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
//@Slf4j
@RequestMapping("/api/reportes-legacy")
public class ReportesDescontinuados {

    private final ReporteSecundarioService reporteSecundarioService;
    private final JdbcTemplate jdbcLegacy; // ← JdbcTemplate ya listo, no DataSource crudo

    // Constructor manual: así @Qualifier funciona correctamente
    public ReportesDescontinuados(
            ReporteSecundarioService reporteSecundarioService,
            @Qualifier("secondaryJdbcTemplate") JdbcTemplate jdbcLegacy) {
        this.reporteSecundarioService = reporteSecundarioService;
        this.jdbcLegacy = jdbcLegacy;
    }

    // ─── Vista ────────────────────────────────────────────────────────────────


    // ─── Descubrimiento de datos ─────────────────────────────────────────────

    @ResponseBody
    @GetMapping("/rango-disponible")
    public ResponseEntity<Map<String, Object>> rangoDisponible() {
        // Excluye años corruptos (0024, 20225, etc.) — solo años razonables
        String sql = """
                SELECT COUNT(*)                                             AS total,
                       TO_CHAR(MIN(fecha_recepcion), 'YYYY-MM-DD')         AS primera_fecha,
                       TO_CHAR(MAX(fecha_recepcion), 'YYYY-MM-DD')         AS ultima_fecha
                FROM servicio
                WHERE estado_servicio = 'T'
                  AND EXTRACT(YEAR FROM fecha_recepcion) BETWEEN 1990 AND 2100
                """;
        return ResponseEntity.ok(jdbcLegacy.queryForMap(sql));
    }

    @ResponseBody
    @GetMapping("/preview-count")
    public ResponseEntity<Map<String, Object>> previewCount(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam(required = false) Long idTipoServicio,
            @RequestParam(required = false) Long idTipoEquipo,
            @RequestParam(required = false) Long idUnidad) {

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) AS total
                FROM servicio s
                LEFT JOIN persona p ON p.id_persona = s.id_persona
                LEFT JOIN unidad u  ON u.id_unidad  = p.id_unidad
                WHERE s.estado_servicio = 'T'
                  AND s.fecha_recepcion BETWEEN ? AND ?
                """);

        List<Object> params = new ArrayList<>(List.of(fechaInicial, fechaFinal));

        if (idTipoEquipo != null)   { sql.append(" AND s.id_tipoequipo = ?");    params.add(idTipoEquipo); }
        if (idTipoServicio != null) { sql.append(" AND s.id_tipo_servicio = ?"); params.add(idTipoServicio); }
        if (idUnidad != null)       { sql.append(" AND u.id_unidad = ?");        params.add(idUnidad); }

        return ResponseEntity.ok(jdbcLegacy.queryForMap(sql.toString(), params.toArray()));
    }

    // ─── Catálogos desde BD antigua ──────────────────────────────────────────

    @ResponseBody
    @GetMapping("/catalogo/usuarios")
    public ResponseEntity<List<Map<String, Object>>> catalogoUsuarios() {

        String sql = """
                SELECT u.id_usuario AS id,
                       p.nombre || ' ' || p.ap_paterno || ' ' || COALESCE(p.ap_materno, '') AS nombre
                FROM usuario u
                JOIN persona p ON p.id_persona = u.id_persona
                WHERE u.estado = 'A'
                ORDER BY p.nombre
                """;

        List<Map<String, Object>> resultadoUsuario = jdbcLegacy.queryForList(sql);

//        log.info("SQL ejecutado:\n{}", sql);
//        log.info("Resultado: {}", resultadoUsuario);

        return ResponseEntity.ok(resultadoUsuario);
    }

    @ResponseBody
    @GetMapping("/catalogo/tipos-servicio")
    public ResponseEntity<List<Map<String, Object>>> catalogoTiposServicio() {
        String sql = """
                SELECT id_tipo_servicio  AS id,
                       nom_tipo_servicio AS nombre
                FROM tiposervicio
                WHERE estado_tipo_servicio = 'A'
                ORDER BY nom_tipo_servicio;
                """;
        List<Map<String, Object>> resultadoServicio = jdbcLegacy.queryForList(sql);
//        log.info("SQL ejecutado:\n{}", sql);
//        log.info("Resultado: {}", resultadoServicio);
        return ResponseEntity.ok(resultadoServicio);
    }

    @ResponseBody
    @GetMapping("/catalogo/tipos-equipo")
    public ResponseEntity<List<Map<String, Object>>> catalogoTiposEquipo() {
        String sql = """
                SELECT id_tipoequipo AS id,
                       equipo AS nombre
                FROM tipoequipo
                ORDER BY equipo
                """;
        List<Map<String, Object>> resultadoTipoEquipo = jdbcLegacy.queryForList(sql);

//        log.info("SQL ejecutado:\n{}", sql);
//        log.info("Resultado tipos equipo: {}", resultadoTipoEquipo);

        return ResponseEntity.ok(resultadoTipoEquipo);
    }

    @ResponseBody
    @GetMapping("/catalogo/unidades")
    public ResponseEntity<List<Map<String, Object>>> catalogoUnidades() {
        String sql = """
                SELECT id_unidad AS id,
                       unidad    AS nombre
                FROM unidad
                ORDER BY unidad
                """;
        List<Map<String, Object>> resultadoUnidad = jdbcLegacy.queryForList(sql);
//        log.info("SQL ejecutado:\n{}", sql);
//        log.info("Resultado tipos equipo: {}", resultadoUnidad);

        return ResponseEntity.ok(resultadoUnidad);
    }

    // ─── Generación de reportes ───────────────────────────────────────────────

    @ResponseBody
    @GetMapping("/reporte-general")
    public ResponseEntity<byte[]> reporteGeneral(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam Long idUsuario) throws Exception {
        byte[] pdf = reporteSecundarioService.exportarReporteGeneral(fechaInicial, fechaFinal, idUsuario);
        return pdfResponse(pdf, "Reporte_General_Legacy.pdf");
    }

    @ResponseBody
    @GetMapping("/reporte-personalizado")
    public ResponseEntity<byte[]> reportePersonalizado(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam Long idUsuario,
            @RequestParam(required = false) Long idTipoServicio,
            @RequestParam(required = false) Long idTipoEquipo,
            @RequestParam(required = false) Long idUnidad) throws Exception {
        byte[] pdf = reporteSecundarioService.exportarReportePersonalizado(
                fechaInicial, fechaFinal, idUsuario, idTipoServicio, idTipoEquipo, idUnidad);
        return pdfResponse(pdf, "Reporte_Personalizado_Legacy.pdf");
    }

    @ResponseBody
    @GetMapping("/reporte-personalizado-2")
    public ResponseEntity<byte[]> reportePersonalizado2(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam Long idUsuario,
            @RequestParam(required = false) Long idTipoServicio,
            @RequestParam(required = false) Long idTipoEquipo) throws Exception {
        byte[] pdf = reporteSecundarioService.exportarReportePersonalizado2(
                fechaInicial, fechaFinal, idUsuario, idTipoServicio, idTipoEquipo);
        return pdfResponse(pdf, "Reporte_Personalizado_2_Legacy.pdf");
    }

    @ResponseBody
    @GetMapping("/reporte-personalizado-3")
    public ResponseEntity<byte[]> reportePersonalizado3(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam Long idUsuario,
            @RequestParam(required = false) Long idTipoServicio) throws Exception {
        byte[] pdf = reporteSecundarioService.exportarReportePersonalizado3(
                fechaInicial, fechaFinal, idUsuario, idTipoServicio);
        return pdfResponse(pdf, "Reporte_Personalizado_3_Legacy.pdf");
    }

    @ResponseBody
    @GetMapping("/reporte-personalizado-4")
    public ResponseEntity<byte[]> reportePersonalizado4(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam Long idUsuario,
            @RequestParam(required = false) Long idTipoEquipo) throws Exception {
        byte[] pdf = reporteSecundarioService.exportarReportePersonalizado4(
                fechaInicial, fechaFinal, idUsuario, idTipoEquipo);
        return pdfResponse(pdf, "Reporte_Personalizado_4_Legacy.pdf");
    }

    @ResponseBody
    @GetMapping("/reporte-servicios-usuario")
    public ResponseEntity<byte[]> reporteServiciosUsuario(
            @RequestParam Long idUsuario,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal) throws Exception {
        byte[] pdf = reporteSecundarioService.exportarReporteServiciosUsuario(
                idUsuario, fechaInicial, fechaFinal);
        return pdfResponse(pdf, "Reporte_Servicios_Usuario_Legacy.pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
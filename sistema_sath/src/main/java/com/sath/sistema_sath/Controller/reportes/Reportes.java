package com.sath.sistema_sath.Controller.reportes;

import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usuario.UsuarioRepository;
import com.sath.sistema_sath.tools.jasper.ReporteService;
import com.sath.sistema_sath.tools.reportes.ReporteGenericoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/reportes")
public class Reportes {

    private final ReporteService reporteService;
    private final UsuarioRepository usuarioRepository;
    private final ReporteGenericoRepository reporteRepo;

    private boolean esAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }


    @ResponseBody
    @GetMapping("/preview/ficha/{nroSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> previewFichaTecnica(@PathVariable int nroSolicitud) {
        List<Map<String, Object>> datos = reporteRepo.previewFichaTecnica(nroSolicitud);
        if (datos.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(datos);
    }

    @ResponseBody
    @GetMapping("/preview/informe-especifico/{idInforme}")
    public ResponseEntity<List<Map<String, Object>>> previewInformeEspecifico(@PathVariable int idInforme) {
        List<Map<String, Object>> datos = reporteRepo.previewInformeEspecifico(idInforme);
        if (datos.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(datos);
    }

    @ResponseBody
    @GetMapping("/preview/informe-general/{idInforme}")
    public ResponseEntity<List<Map<String, Object>>> previewInformeGeneral(@PathVariable int idInforme) {
        List<Map<String, Object>> datos = reporteRepo.previewInformeGeneral(idInforme);
        if (datos.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(datos);
    }

    @ResponseBody
    @GetMapping("/informe-especifico/{idInforme}")
    public ResponseEntity<byte[]> descargarInformeEspecifico(@PathVariable Long idInforme) throws Exception {
        byte[] pdf = reporteService.exportarInformeEspecifico(idInforme);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Informe_Especifico_" + idInforme + ".pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/informe-general/{idInforme}")
    public ResponseEntity<byte[]> descargarInformeGeneral(@PathVariable Long idInforme) throws Exception {
        byte[] pdf = reporteService.exportarInformeGeneral(idInforme);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Informe_General_" + idInforme + ".pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    // api/reportes/reporte-general?fechaInicial=yyyy-MM-dd&fechaFinal=yyyy-MM-dd&idAdministrativo=X
    @ResponseBody
    @GetMapping("/reporte-general")
    public ResponseEntity<byte[]> descargarReporteGeneral(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam Long idAdministrativo) throws Exception {
        byte[] pdf = reporteService.exportarReporteGeneral(fechaInicial, fechaFinal, idAdministrativo);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Reporte_General_SATHv2.pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/reporte-general-excel")
    @ResponseBody
    public ResponseEntity<byte[]> reporteGeneralExcel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date fechaFinal,
            @RequestParam(required = false) Long idAdministrativo) throws Exception {

        byte[] excel = reporteService.exportarReporteGeneralExcel(fechaInicial, fechaFinal, idAdministrativo);

        return ResponseEntity.ok()
                // ↓ CAMBIO 3: Content-Type de Excel y extensión .xlsx
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Reporte_SATH.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // api/reportes/ficha-tecnica/{nroSolicitud}?observacion=...
    @ResponseBody
    @GetMapping("/ficha-tecnica/{nroSolicitud}")
    public ResponseEntity<byte[]> descargarFicha(
            @PathVariable Long nroSolicitud,
            @RequestParam(required = false) String observacion) throws Exception {

        // ── Usuario autenticado ─────────────────────────────────────────
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Long idUsuario = usuario.getIdUsuario();
        String nombreTecnico = usuario.getPersona().getNombreCompleto();
        // ───────────────────────────────────────────────────────────────
        byte[] pdf = reporteService.exportarFichaTecnicaConQR(
                nroSolicitud,
                observacion,
                idUsuario,
                nombreTecnico,
                esAdmin()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Ficha_Tecnica_" + nroSolicitud + ".pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    private static final MediaType DOCX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    @ResponseBody
    @GetMapping("/ficha-tecnica-word/{nroSolicitud}")
    public ResponseEntity<byte[]> descargarFichaWord(
            @PathVariable Long nroSolicitud,
            @RequestParam(required = false) String observacion) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        byte[] docx = reporteService.exportarFichaTecnicaComoWord(
                nroSolicitud, observacion, usuario.getIdUsuario(), usuario.getPersona().getNombreCompleto(), esAdmin());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(DOCX);
        headers.setContentDispositionFormData("attachment", "Ficha_Tecnica_" + nroSolicitud + ".docx");
        headers.setContentLength(docx.length);
        return new ResponseEntity<>(docx, headers, HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/reporte-personalizado")
    public ResponseEntity<byte[]> descargarReportePersonalizado(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam(required = false) Long idUnidad,
            @RequestParam(required = false) Long idTipoServicio,
            @RequestParam(required = false) Long idTipoActivo,
            @RequestParam(required = false) Long idAdministrativo) throws Exception {

        byte[] pdf = reporteService.exportarReportePersonalizado(
                fechaInicial, fechaFinal, idUnidad, idTipoServicio, idTipoActivo, idAdministrativo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Reporte_Personalizado_SATH.pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/reporte-personalizado-excel")
    public ResponseEntity<byte[]> descargarReportePersonalizadoExcel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam(required = false) Long idUnidad,
            @RequestParam(required = false) Long idTipoServicio,
            @RequestParam(required = false) Long idTipoActivo,
            @RequestParam(required = false) Long idAdministrativo) throws Exception {

        byte[] excel = reporteService.exportarReportePersonalizadoExcel(
                fechaInicial, fechaFinal, idUnidad, idTipoServicio, idTipoActivo, idAdministrativo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Personalizado_SATH.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @ResponseBody
    @GetMapping("/ficha-tecnica-servicio-word/{nroSolicitud}")
    public ResponseEntity<byte[]> descargarFichaServicioWord(
            @PathVariable Long nroSolicitud,
            @RequestParam(required = false) String observacion) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        byte[] docx = reporteService.exportarFichaTecnicaServicioComoWord(
                nroSolicitud, observacion, usuario.getIdUsuario(), usuario.getPersona().getNombreCompleto(), esAdmin());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(DOCX);
        headers.setContentDispositionFormData("attachment", "Ficha_Servicio_Tecnico_" + nroSolicitud + ".docx");
        headers.setContentLength(docx.length);
        return new ResponseEntity<>(docx, headers, HttpStatus.OK);
    }

    // api/reportes/reporte-estadistico-servicios?fechaInicial=yyyy-MM-dd&fechaFinal=yyyy-MM-dd&idAdministrativo=X
    @ResponseBody
    @GetMapping("/reporte-estadistico-servicios")
    public ResponseEntity<byte[]> descargarReporteEstadisticoServicios(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam(required = false) Long idAdministrativo) throws Exception {

        byte[] pdf = reporteService.exportarReporteEstadisticoServicios(
                fechaInicial, fechaFinal, idAdministrativo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Reporte_Estadistico_Servicios.pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/reporte-estadistico-servicios-excel")
    public ResponseEntity<byte[]> descargarReporteEstadisticoServiciosExcel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicial,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFinal,
            @RequestParam(required = false) Long idAdministrativo) throws Exception {

        byte[] excel = reporteService.exportarReporteEstadisticoServiciosExcel(
                fechaInicial, fechaFinal, idAdministrativo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Estadistico_Servicios.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // api/reportes/ficha-tecnica-servicio/{nroSolicitud}?observacion=...
    @ResponseBody
    @GetMapping("/ficha-tecnica-servicio/{nroSolicitud}")
    public ResponseEntity<byte[]> descargarFichaServicioTecnico(
            @PathVariable Long nroSolicitud,
            @RequestParam(required = false) String observacion) throws Exception {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Long idUsuario = usuario.getIdUsuario();
        String nombreTecnico = usuario.getPersona().getNombreCompleto();

        byte[] pdf = reporteService.exportarFichaTecnicaConQRServicioTecnico(
                nroSolicitud, observacion, idUsuario, nombreTecnico, esAdmin());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Ficha_Servicio_Tecnico_" + nroSolicitud + ".pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}

package com.sath.sistema_sath.Controller.solicitud;

import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import com.sath.sistema_sath.domain.solicitud.manual.SolicitudPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/solicitud")
@RequiredArgsConstructor
public class SolicitudPdfController {

    private final SolicitudPdfService pdfService;
    private final SolicitudRepository solicitudRepo;

    @GetMapping("/{id}/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) throws Exception {
        var solicitud = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));

        byte[] pdf = pdfService.generarPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Solicitud_" + solicitud.getNroSol() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
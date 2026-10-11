package com.sath.sistema_sath.domain.solicitud.manual;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SolicitudPdfService {

    private final DataSource dataSource;
    private ConcurrentHashMap<String, JasperReport> cache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, byte[]> imagenCache = new ConcurrentHashMap<>();

    public byte[] generarPdf(Long idSolicitud) throws Exception {
        JasperReport compiled = cache.computeIfAbsent("/reports/Solicitud.jrxml", path -> {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) throw new RuntimeException("No se encontró: " + path);
                return JasperCompileManager.compileReport(is);
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        InputStream logo = getImagen("/imagesForReports/logo_principal.png");

        Map<String, Object> params = new HashMap<>();
        params.put("id_solicitud", idSolicitud);
        params.put("logoPrincipal", logo);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(compiled, params, con);
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();
        }
        return out.toByteArray();
    }

    private InputStream getImagen(String path) {
        byte[] bytes = imagenCache.computeIfAbsent(path, p -> {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                return is == null ? new byte[0] : is.readAllBytes();
            } catch (Exception e) { return new byte[0]; }
        });
        return bytes.length == 0 ? null : new ByteArrayInputStream(bytes);
    }
}

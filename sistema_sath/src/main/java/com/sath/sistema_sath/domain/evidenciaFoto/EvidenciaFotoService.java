package com.sath.sistema_sath.domain.evidenciaFoto;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.Tools.FileStorageProperties;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvidenciaFotoService {

    private static final int MAX_FOTOS = 5;
    private static final List<String> FORMATOS_PERMITIDOS = Arrays.asList("jpg", "jpeg", "png", "pdf");
    private static final String SUBCARPETA = "evidencias";

    private final EvidenciaFotoRepository repository;
    private final FileStorageProperties storageProperties;
    private final SolicitudRepository solicitudRepository;

    public Map<String, Object> subir(Long idSolicitud, MultipartFile archivo, String descripcion) throws IOException {
        long total = repository.countBySolicitudIdSolicitudAndEstadoNot(idSolicitud, EstadoEnum.ELIMINADO);
        if (total >= MAX_FOTOS) {
            throw new RuntimeException("Límite de " + MAX_FOTOS + " fotos por solicitud alcanzado");
        }

        String extension = obtenerExtension(archivo.getOriginalFilename());
        if (!FORMATOS_PERMITIDOS.contains(extension.toLowerCase())) {
            throw new RuntimeException("Formato no permitido. Use: " + FORMATOS_PERMITIDOS);
        }

        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + idSolicitud));

        Path dir = Paths.get(storageProperties.getArchivos(), SUBCARPETA).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String nombreArchivo = "sol_" + idSolicitud + "_" + UUID.randomUUID() + "." + extension;
        Path destino = dir.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        EvidenciaFoto foto = new EvidenciaFoto();
        foto.setSolicitud(solicitud);
        foto.setRutaArchivo(nombreArchivo);
        foto.setNombreOriginal(archivo.getOriginalFilename());
        foto.setDescripcion(descripcion);
        EvidenciaFoto guardada = repository.save(foto);

        return Map.of(
                "idEvidenciaFoto", guardada.getIdEvidenciaFoto(),
                "nombreOriginal", guardada.getNombreOriginal() != null ? guardada.getNombreOriginal() : "",
                "descripcion", guardada.getDescripcion() != null ? guardada.getDescripcion() : "",
                "total", total + 1
        );
    }

    public List<Map<String, Object>> listar(Long idSolicitud) {
        return repository.findBySolicitudIdSolicitudAndEstadoNot(idSolicitud, EstadoEnum.ELIMINADO)
                .stream()
                .map(f -> Map.<String, Object>of(
                        "idEvidenciaFoto", f.getIdEvidenciaFoto(),
                        "nombreOriginal", f.getNombreOriginal() != null ? f.getNombreOriginal() : "",
                        "descripcion", f.getDescripcion() != null ? f.getDescripcion() : "",
                        "fechaSubida", f.getFechaRegistro() != null ? f.getFechaRegistro().toString() : ""
                ))
                .toList();
    }

    public void eliminar(Long idEvidenciaFoto) throws IOException {
        EvidenciaFoto foto = repository.findById(idEvidenciaFoto)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada: " + idEvidenciaFoto));

        Path archivo = Paths.get(storageProperties.getArchivos(), SUBCARPETA, foto.getRutaArchivo())
                .toAbsolutePath().normalize();
        Files.deleteIfExists(archivo);

        foto.setEstado(EstadoEnum.ELIMINADO);
        repository.save(foto);
    }

    public byte[] obtenerBytes(Long idEvidenciaFoto) throws IOException {
        EvidenciaFoto foto = repository.findById(idEvidenciaFoto)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada: " + idEvidenciaFoto));
        Path archivo = Paths.get(storageProperties.getArchivos(), SUBCARPETA, foto.getRutaArchivo())
                .toAbsolutePath().normalize();
        return Files.readAllBytes(archivo);
    }

    public List<byte[]> obtenerBytesParaSolicitud(Long idSolicitud) {
        return repository.findBySolicitudIdSolicitudAndEstadoNot(idSolicitud, EstadoEnum.ELIMINADO)
                .stream()
                .map(f -> {
                    try {
                        Path archivo = Paths.get(storageProperties.getArchivos(), SUBCARPETA, f.getRutaArchivo())
                                .toAbsolutePath().normalize();
                        return Files.readAllBytes(archivo);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(b -> b != null)
                .toList();
    }

    public String obtenerContentType(Long idEvidenciaFoto) {
        EvidenciaFoto foto = repository.findById(idEvidenciaFoto)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada"));
        String ext = obtenerExtension(foto.getRutaArchivo()).toLowerCase();
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            default -> "image/jpeg";
        };
    }

    private String obtenerExtension(String nombre) {
        if (nombre == null || !nombre.contains(".")) return "jpg";
        return nombre.substring(nombre.lastIndexOf('.') + 1);
    }
}
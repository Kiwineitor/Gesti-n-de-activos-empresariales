package com.sath.sistema_sath.Tools;


import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FileStorageService {
//    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    // Formatos soportados
    private static final List<String> AUDIO_FORMATS = Arrays.asList("wav", "flac", "ogg", "aac", "m4a", "mp3");
    private static final List<String> VIDEO_FORMATS = Arrays.asList("avi", "mov", "mkv", "wmv", "flv", "mp4");
    private static final List<String> DOCUMENT_FORMATS = Arrays.asList("doc", "docx", "xls", "xlsx", "txt", "rtf");
    private static final List<String> IMAGE_FORMATS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    private static final List<String> PDF_FORMAT = Arrays.asList("pdf");

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getArchivos())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
//            logger.info("✅ Directorio de almacenamiento creado/verificado: {}", this.fileStorageLocation);
        } catch (Exception ex) {
//            logger.error("❌ Error al crear directorio de almacenamiento", ex);
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento", ex);
        }

        try {
            this.ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            this.ffprobe = new FFprobe("/usr/bin/ffprobe");
//            logger.info("✅ FFmpeg y FFprobe cargados correctamente");
        } catch (IOException e) {
//            logger.error("❌ Error al cargar FFmpeg/FFprobe", e);
            throw new RuntimeException("No se pudieron cargar FFmpeg o FFprobe. Asegúrate de que estén instalados en /usr/bin/", e);
        }
    }

    // ==================== MÉTODOS PÚBLICOS PRINCIPALES ====================

    /**
     * Guarda un archivo en la ruta base (sin subcarpeta)
     */
    public String storeFile(MultipartFile file) {
        return storeFileInSubfolder(file, "");
    }

    /**
     * Guarda un archivo dentro de una subcarpeta (ej. "imagenes", "documentos")
     */
    public String storeFileInSubfolder(MultipartFile file, String subfolder) {
        validateSubfolder(subfolder);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        validateFileName(originalName);
        validateFileSize(file);

        String lowerName = originalName.toLowerCase();
        String extension = getFileExtension(lowerName);

        if (!isFormatSupported(extension)) {
            throw new RuntimeException("Formato no soportado: " + extension);
        }

        Path subfolderPath = getSubfolderPath(subfolder);
        try {
            Files.createDirectories(subfolderPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la subcarpeta: " + subfolder, e);
        }

        Path tempInput = null;
        Path finalOutput = null;

        try {
//            logger.info("📤 Procesando archivo en subcarpeta '{}': {}", subfolder, originalName);

            tempInput = Files.createTempFile("upload_", "_" + sanitizeFileName(originalName));
            Files.copy(file.getInputStream(), tempInput, StandardCopyOption.REPLACE_EXISTING);
//            logger.debug("Archivo temporal creado: {}", tempInput);

            finalOutput = convertToStandardFormat(tempInput, lowerName);

            String outputFileName = buildOutputFileName(originalName, finalOutput.getFileName().toString());
            Path target = subfolderPath.resolve(outputFileName);
            Files.move(finalOutput, target, StandardCopyOption.REPLACE_EXISTING);

//            logger.info("✅ Archivo guardado en {}/{}", subfolder, outputFileName);
            return outputFileName;

        } catch (Exception e) {
//            logger.error("❌ Error al procesar archivo en subcarpeta '{}': {}", subfolder, originalName, e);
            throw new RuntimeException("Error al procesar archivo: " + e.getMessage(), e);
        } finally {
            cleanupTempFiles(tempInput, finalOutput);
        }
    }

    /**
     * Carga un archivo desde la ruta base (sin subcarpeta)
     */
    public Resource loadFileAsResource(String fileName) {
        return loadFileAsResource(fileName, "");
    }

    /**
     * Carga un archivo desde una subcarpeta específica
     */
    public Resource loadFileAsResource(String fileName, String subfolder) {
        validateSubfolder(subfolder);
        Path subfolderPath = getSubfolderPath(subfolder);
        Path filePath = subfolderPath.resolve(fileName).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
//                logger.info("📥 Archivo cargado desde {}/{} para descarga", subfolder.isEmpty() ? "(raíz)" : subfolder, fileName);
                return resource;
            } else {
//                logger.warn("⚠️ Archivo no encontrado o no legible en {}/{}", subfolder.isEmpty() ? "(raíz)" : subfolder, fileName);
                throw new RuntimeException("Archivo no encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
//            logger.error("❌ Error al cargar archivo desde {}/{}", subfolder.isEmpty() ? "(raíz)" : subfolder, fileName, ex);
            throw new RuntimeException("Error al cargar archivo: " + fileName, ex);
        }
    }

    /**
     * Verifica si un archivo existe en una subcarpeta
     */
    public boolean fileExists(String fileName, String subfolder) {
        validateSubfolder(subfolder);
        Path filePath = getSubfolderPath(subfolder).resolve(fileName).normalize();
        return Files.exists(filePath);
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    public String getContentType(String fileName) {
        String extension = getFileExtension(fileName.toLowerCase());
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "mp3" -> "audio/mpeg";
            case "mp4" -> "video/mp4";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            default -> "application/octet-stream";
        };
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private Path getSubfolderPath(String subfolder) {
        if (subfolder == null || subfolder.trim().isEmpty()) {
            return fileStorageLocation;
        }
        return fileStorageLocation.resolve(subfolder.trim()).normalize();
    }

    private void validateSubfolder(String subfolder) {
        if (subfolder == null) return;
        String clean = subfolder.trim();
        if (clean.contains("..") || clean.startsWith("/") || clean.startsWith("\\")) {
            throw new RuntimeException("Subcarpeta no segura: " + subfolder);
        }
    }

    private void validateFileName(String fileName) {
        if (fileName.contains("..")) {
            throw new RuntimeException("Nombre de archivo inválido (path traversal): " + fileName);
        }
        if (fileName.trim().isEmpty()) {
            throw new RuntimeException("Nombre de archivo vacío");
        }
    }

    private void validateFileSize(MultipartFile file) {
        long maxSize = 500 * 1024 * 1024; // 500MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Archivo demasiado grande. Máximo: 500MB");
        }
        if (file.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }
    }

    private boolean isFormatSupported(String extension) {
        return AUDIO_FORMATS.contains(extension) ||
                VIDEO_FORMATS.contains(extension) ||
                DOCUMENT_FORMATS.contains(extension) ||
                IMAGE_FORMATS.contains(extension) ||
                PDF_FORMAT.contains(extension);
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot > 0) ? fileName.substring(lastDot + 1) : "";
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String buildOutputFileName(String originalName, String convertedFileName) {
        String extension = getFileExtension(convertedFileName);
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        String timestamp = String.valueOf(System.currentTimeMillis());
        return sanitizeFileName(baseName) + "_" + timestamp + "." + extension;
    }

    private void cleanupTempFiles(Path... paths) {
        for (Path path : paths) {
            try {
                if (path != null && Files.exists(path)) {
                    Files.deleteIfExists(path);
//                    logger.debug("Archivo temporal eliminado: {}", path);
                }
            } catch (IOException e) {
                //  logger.warn("No se pudo eliminar archivo temporal: {}", path, e);
            }
        }
    }

    // ==================== MÉTODOS DE CONVERSIÓN (sin cambios) ====================

    private Path convertToStandardFormat(Path inputFile, String originalName) throws IOException {
        String extension = getFileExtension(originalName);
        Path output;

        if (PDF_FORMAT.contains(extension) || "mp3".equals(extension) || "mp4".equals(extension)) {
            //   logger.debug("Archivo ya está en formato estándar: {}", extension);
            return inputFile;
        }

        if (DOCUMENT_FORMATS.contains(extension) || IMAGE_FORMATS.contains(extension)) {
            output = Files.createTempFile("converted_", ".pdf");
            convertToPdf(inputFile, output);
            //    logger.info("✅ Convertido a PDF");
            return output;
        }

        if (AUDIO_FORMATS.contains(extension)) {
            output = Files.createTempFile("converted_", ".mp3");
            convertAudioToMp3(inputFile, output);
            //  logger.info("✅ Convertido a MP3");
            return output;
        }

        if (VIDEO_FORMATS.contains(extension)) {
            output = Files.createTempFile("converted_", ".mp4");
            convertVideoToMp4(inputFile, output);
            //    logger.info("✅ Convertido a MP4");
            return output;
        }

        throw new RuntimeException("Formato no soportado para conversión: " + originalName);
    }

    private void convertAudioToMp3(Path input, Path output) throws IOException {
        try {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(input.toString())
                    .overrideOutputFiles(true)
                    .addOutput(output.toString())
                    .setAudioCodec("libmp3lame")
                    .setAudioBitRate(192000)
                    .setAudioSampleRate(44100)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
            //  logger.debug("Audio convertido a MP3: {}", output);
        } catch (Exception e) {
            //    logger.error("Error en conversión de audio", e);
            throw new IOException("Error al convertir audio a MP3: " + e.getMessage(), e);
        }
    }

    private void convertVideoToMp4(Path input, Path output) throws IOException {
        try {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(input.toString())
                    .overrideOutputFiles(true)
                    .addOutput(output.toString())
                    .setVideoCodec("libx264")
                    .setVideoFrameRate(30)
                    .setVideoResolution(1280, 720)
                    .setAudioCodec("aac")
                    .setAudioBitRate(128000)
                    .setAudioChannels(2)
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
            // logger.debug("Video convertido a MP4: {}", output);
        } catch (Exception e) {
            //   logger.error("Error en conversión de video", e);
            throw new IOException("Error al convertir video a MP4: " + e.getMessage(), e);
        }
    }

    private void convertToPdf(Path input, Path output) throws IOException {
        String inputStr = input.toAbsolutePath().toString();
        String outputDir = output.getParent().toAbsolutePath().toString();

        ProcessBuilder pb = new ProcessBuilder(
                "libreoffice", "--headless", "--convert-to", "pdf",
                "--outdir", outputDir, inputStr
        );

        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            int exitCode = p.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("LibreOffice falló al convertir a PDF. Código de salida: " + exitCode);
            }

            String baseName = input.getFileName().toString();
            int lastDot = baseName.lastIndexOf('.');
            String pdfName = (lastDot > 0 ? baseName.substring(0, lastDot) : baseName) + ".pdf";
            Path libreOutput = output.getParent().resolve(pdfName);

            if (!Files.exists(libreOutput)) {
                throw new RuntimeException("PDF no generado por LibreOffice");
            }

            Files.move(libreOutput, output, StandardCopyOption.REPLACE_EXISTING);
            // logger.debug("Documento convertido a PDF: {}", output);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Conversión interrumpida", e);
        }
    }
    // En FileStorageService.java

    /**
     * Elimina un archivo de una subcarpeta específica
     */
    public boolean deleteFile(String fileName, String subfolder) {
        validateSubfolder(subfolder);
        Path subfolderPath = getSubfolderPath(subfolder);
        Path filePath = subfolderPath.resolve(fileName).normalize();

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                //     logger.info("🗑️ Archivo eliminado: {}/{}", subfolder.isEmpty() ? "(raíz)" : subfolder, fileName);
                return true;
            } else {
                //   logger.warn("⚠️ Intento de eliminar archivo inexistente: {}/{}", subfolder.isEmpty() ? "(raíz)" : subfolder, fileName);
                return false;
            }
        } catch (IOException e) {
            //   logger.error("❌ Error al eliminar archivo {}/{}", subfolder.isEmpty() ? "(raíz)" : subfolder, fileName, e);
            return false;
        }
    }
}

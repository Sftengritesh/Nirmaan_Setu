package com.nirmaansetu.operations.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {

    private final Path uploadDir;

    public FileUploadController(@org.springframework.beans.factory.annotation.Value("${nirmaansetu.upload.dir:uploads}") String uploadDirPath) {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directory: " + this.uploadDir, e);
        }
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are permitted"));
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
        String extension = "";
        int dotIdx = originalName.lastIndexOf('.');
        if (dotIdx >= 0) {
            extension = originalName.substring(dotIdx);
        } else {
            extension = ".jpg";
        }

        String filename = UUID.randomUUID() + extension;
        Path targetPath = uploadDir.resolve(filename);

        try {
            file.transferTo(targetPath.toFile());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", "/api/uploads/" + filename));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/base64")
    public ResponseEntity<Map<String, String>> uploadBase64(@RequestBody Map<String, String> payload) {
        String dataUrl = payload.get("dataUrl");
        if (dataUrl == null || dataUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing dataUrl"));
        }

        String base64Data = dataUrl;
        String extension = ".jpg";
        if (dataUrl.contains(",")) {
            String[] parts = dataUrl.split(",");
            base64Data = parts[1];
            if (parts[0].contains("image/png")) {
                extension = ".png";
            } else if (parts[0].contains("image/webp")) {
                extension = ".webp";
            }
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(base64Data);
            String filename = UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(filename);

            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                fos.write(decoded);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", "/api/uploads/" + filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid base64 payload"));
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

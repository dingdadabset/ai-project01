package com.aiproject.module.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * File Upload Controller
 * Handles file uploads for backgrounds, images, and markdown content
 */
@Slf4j
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    @Value("${upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private DataSize maxFileSize;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    /**
     * Upload a single file
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "images") String type) {
        log.info("POST /api/uploads - type: {}, file: {}", type, file.getOriginalFilename());
        
        try {
            validateFile(file);
            String savedPath = saveFile(file, type);
            String url = "/api/uploads/" + type + "/" + Paths.get(savedPath).getFileName().toString();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("url", url);
            result.put("path", savedPath);
            result.put("name", file.getOriginalFilename());
            result.put("size", file.getSize());
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (IOException e) {
            log.error("Failed to save file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Failed to save file"
            ));
        }
    }

    /**
     * Upload multiple files
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "type", defaultValue = "images") String type) {
        log.info("POST /api/uploads/batch - type: {}, count: {}", type, files.length);
        
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                validateFile(file);
                String savedPath = saveFile(file, type);
                String url = "/api/uploads/" + type + "/" + Paths.get(savedPath).getFileName().toString();
                
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("path", savedPath);
                result.put("name", file.getOriginalFilename());
                result.put("size", file.getSize());
                results.add(result);
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", errors.isEmpty());
        response.put("files", results);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get uploaded file
     */
    @GetMapping("/{type}/{filename}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            Path filePath = getUploadPath().resolve(type).resolve(filename);
            
            // Security check: prevent path traversal
            if (!filePath.normalize().startsWith(getUploadPath().normalize())) {
                return ResponseEntity.notFound().build();
            }
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
        } catch (IOException e) {
            log.error("Failed to get file: {}/{}", type, filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * List files in a directory
     */
    @GetMapping("/list/{type}")
    public ResponseEntity<Map<String, Object>> listFiles(@PathVariable String type) {
        try {
            Path typePath = getUploadPath().resolve(type);
            if (!Files.exists(typePath)) {
                Files.createDirectories(typePath);
            }
            
            List<Map<String, Object>> files = new ArrayList<>();
            try (var stream = Files.list(typePath)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    try {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", path.getFileName().toString());
                        fileInfo.put("url", "/api/uploads/" + type + "/" + path.getFileName().toString());
                        fileInfo.put("size", Files.size(path));
                        files.add(fileInfo);
                    } catch (IOException e) {
                        log.warn("Failed to get file info: {}", path, e);
                    }
                });
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "files", files
            ));
        } catch (IOException e) {
            log.error("Failed to list files: {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Failed to list files"
            ));
        }
    }

    /**
     * Delete a file
     */
    @DeleteMapping("/{type}/{filename}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            Path filePath = getUploadPath().resolve(type).resolve(filename);
            
            // Security check: prevent path traversal
            if (!filePath.normalize().startsWith(getUploadPath().normalize())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid path"
                ));
            }
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Files.delete(filePath);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "File deleted successfully"
            ));
        } catch (IOException e) {
            log.error("Failed to delete file: {}/{}", type, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Failed to delete file"
            ));
        }
    }

    // ==================== Private Helper Methods ====================

    private Path getUploadPath() {
        return Paths.get(System.getProperty("user.dir"), uploadDirectory);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        long maxBytes = maxFileSize.toBytes();
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + maxFileSize.toMegabytes() + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + ALLOWED_IMAGE_EXTENSIONS);
        }
    }

    private String saveFile(MultipartFile file, String type) throws IOException {
        Path uploadPath = getUploadPath().resolve(type);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
        
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filePath.toString();
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }
}

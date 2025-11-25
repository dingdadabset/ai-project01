package com.aiproject.module.attachment.controller;

import com.aiproject.module.attachment.model.Attachment;
import com.aiproject.module.attachment.service.AttachmentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Attachment Controller
 * REST API endpoints for attachments
 */
@Slf4j
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Create a new attachment
     */
    @PostMapping
    public ResponseEntity<Attachment> createAttachment(@RequestBody Map<String, Object> request) {
        log.info("POST /api/attachments - Creating new attachment");
        Attachment.AttachmentType type = request.get("type") != null ?
                Attachment.AttachmentType.valueOf((String) request.get("type")) : Attachment.AttachmentType.OTHER;
        Long uploaderId = ((Number) request.get("uploaderId")).longValue();
        Long size = ((Number) request.get("size")).longValue();
        Integer width = request.get("width") != null ? ((Number) request.get("width")).intValue() : 0;
        Integer height = request.get("height") != null ? ((Number) request.get("height")).intValue() : 0;
        
        Attachment attachment = attachmentService.createAttachment(
                (String) request.get("name"),
                (String) request.get("path"),
                (String) request.get("url"),
                (String) request.get("mediaType"),
                (String) request.get("suffix"),
                size,
                width,
                height,
                uploaderId,
                type
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
    }

    /**
     * Get attachment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Attachment> getAttachmentById(@PathVariable Long id) {
        log.info("GET /api/attachments/{}", id);
        Attachment attachment = attachmentService.getAttachmentById(id);
        return ResponseEntity.ok(attachment);
    }

    /**
     * List all attachments
     */
    @GetMapping
    public ResponseEntity<List<Attachment>> getAllAttachments() {
        log.info("GET /api/attachments");
        List<Attachment> attachments = attachmentService.getAllAttachments();
        return ResponseEntity.ok(attachments);
    }

    /**
     * List attachments with pagination
     */
    @GetMapping("/page")
    public ResponseEntity<IPage<Attachment>> listAttachments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/attachments/page - page: {}, size: {}", page, size);
        IPage<Attachment> attachments = attachmentService.listAttachments(page, size);
        return ResponseEntity.ok(attachments);
    }

    /**
     * List attachments by uploader
     */
    @GetMapping("/uploader/{uploaderId}")
    public ResponseEntity<IPage<Attachment>> listAttachmentsByUploader(
            @PathVariable Long uploaderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/attachments/uploader/{} - page: {}, size: {}", uploaderId, page, size);
        IPage<Attachment> attachments = attachmentService.listAttachmentsByUploader(uploaderId, page, size);
        return ResponseEntity.ok(attachments);
    }

    /**
     * List attachments by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<IPage<Attachment>> listAttachmentsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/attachments/type/{} - page: {}, size: {}", type, page, size);
        Attachment.AttachmentType attachmentType = Attachment.AttachmentType.valueOf(type.toUpperCase());
        IPage<Attachment> attachments = attachmentService.listAttachmentsByType(attachmentType, page, size);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Update an attachment
     */
    @PutMapping("/{id}")
    public ResponseEntity<Attachment> updateAttachment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/attachments/{}", id);
        Attachment attachment = attachmentService.updateAttachment(id, request.get("name"));
        return ResponseEntity.ok(attachment);
    }

    /**
     * Delete an attachment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        log.info("DELETE /api/attachments/{}", id);
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}

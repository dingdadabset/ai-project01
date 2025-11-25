package com.aiproject.module.tag.controller;

import com.aiproject.module.tag.model.Tag;
import com.aiproject.module.tag.service.TagService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Tag Controller
 * REST API endpoints for tags
 */
@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * Create a new tag
     */
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Map<String, String> request) {
        log.info("POST /api/tags - Creating new tag");
        Tag tag = tagService.createTag(request.get("name"));
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    /**
     * Get tag by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        log.info("GET /api/tags/{}", id);
        Tag tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    /**
     * Get tag by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Tag> getTagBySlug(@PathVariable String slug) {
        log.info("GET /api/tags/slug/{}", slug);
        Tag tag = tagService.getTagBySlug(slug);
        return ResponseEntity.ok(tag);
    }

    /**
     * List all tags
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        log.info("GET /api/tags");
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * List tags with pagination
     */
    @GetMapping("/page")
    public ResponseEntity<IPage<Tag>> listTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/tags/page - page: {}, size: {}", page, size);
        IPage<Tag> tags = tagService.listTags(page, size);
        return ResponseEntity.ok(tags);
    }

    /**
     * Update a tag
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/tags/{}", id);
        Tag tag = tagService.updateTag(id, request.get("name"));
        return ResponseEntity.ok(tag);
    }

    /**
     * Delete a tag
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("DELETE /api/tags/{}", id);
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}

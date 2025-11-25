package com.aiproject.module.page.controller;

import com.aiproject.module.page.model.Page;
import com.aiproject.module.page.service.PageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Page Controller
 * REST API endpoints for static pages
 */
@Slf4j
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    /**
     * Create a new page
     */
    @PostMapping
    public ResponseEntity<Page> createPage(@RequestBody Map<String, Object> request) {
        log.info("POST /api/pages - Creating new page");
        Page.PageStatus status = request.get("status") != null ?
                Page.PageStatus.valueOf((String) request.get("status")) : Page.PageStatus.DRAFT;
        Long authorId = ((Number) request.get("authorId")).longValue();
        Page page = pageService.createPage(
                (String) request.get("title"),
                (String) request.get("content"),
                (String) request.get("originalContent"),
                authorId,
                status
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(page);
    }

    /**
     * Get page by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Page> getPageById(@PathVariable Long id) {
        log.info("GET /api/pages/{}", id);
        Page page = pageService.getPageById(id);
        pageService.incrementViewCount(id);
        return ResponseEntity.ok(page);
    }

    /**
     * Get page by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Page> getPageBySlug(@PathVariable String slug) {
        log.info("GET /api/pages/slug/{}", slug);
        Page page = pageService.getPageBySlug(slug);
        pageService.incrementViewCount(page.getId());
        return ResponseEntity.ok(page);
    }

    /**
     * List all pages
     */
    @GetMapping
    public ResponseEntity<List<Page>> getAllPages() {
        log.info("GET /api/pages");
        List<Page> pages = pageService.getAllPages();
        return ResponseEntity.ok(pages);
    }

    /**
     * List pages with pagination
     */
    @GetMapping("/list")
    public ResponseEntity<IPage<Page>> listPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/pages/list - page: {}, size: {}", page, size);
        IPage<Page> pages = pageService.listPages(page, size);
        return ResponseEntity.ok(pages);
    }

    /**
     * List published pages
     */
    @GetMapping("/published")
    public ResponseEntity<IPage<Page>> listPublishedPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/pages/published - page: {}, size: {}", page, size);
        IPage<Page> pages = pageService.listPublishedPages(page, size);
        return ResponseEntity.ok(pages);
    }

    /**
     * Update a page
     */
    @PutMapping("/{id}")
    public ResponseEntity<Page> updatePage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/pages/{}", id);
        Page.PageStatus status = request.get("status") != null ?
                Page.PageStatus.valueOf((String) request.get("status")) : null;
        Page page = pageService.updatePage(id,
                (String) request.get("title"),
                (String) request.get("content"),
                (String) request.get("originalContent"),
                status);
        return ResponseEntity.ok(page);
    }

    /**
     * Delete a page
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        log.info("DELETE /api/pages/{}", id);
        pageService.deletePage(id);
        return ResponseEntity.noContent().build();
    }
}

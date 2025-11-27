package com.aiproject.module.externaltool.controller;

import com.aiproject.module.externaltool.model.ExternalTool;
import com.aiproject.module.externaltool.service.ExternalToolService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * External Tool Controller
 * REST API endpoints for external tools management
 */
@Slf4j
@RestController
@RequestMapping("/api/external-tools")
@RequiredArgsConstructor
public class ExternalToolController {

    private final ExternalToolService externalToolService;

    /**
     * Create a new external tool
     */
    @PostMapping
    public ResponseEntity<?> createTool(@RequestBody Map<String, Object> request) {
        log.info("POST /api/external-tools - Creating new tool");
        
        ExternalTool.ToolCategory category = null;
        if (request.get("category") != null) {
            try {
                category = ExternalTool.ToolCategory.valueOf((String) request.get("category"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid category: " + request.get("category")));
            }
        }
        
        Integer displayOrder = null;
        Object displayOrderObj = request.get("displayOrder");
        if (displayOrderObj != null) {
            if (displayOrderObj instanceof Number) {
                displayOrder = ((Number) displayOrderObj).intValue();
            }
        }
        
        ExternalTool tool = externalToolService.createTool(
                (String) request.get("name"),
                (String) request.get("description"),
                (String) request.get("url"),
                (String) request.get("icon"),
                (String) request.get("iconBgColor"),
                category,
                displayOrder
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(tool);
    }

    /**
     * Get tool by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExternalTool> getToolById(@PathVariable Long id) {
        log.info("GET /api/external-tools/{}", id);
        ExternalTool tool = externalToolService.getToolById(id);
        return ResponseEntity.ok(tool);
    }

    /**
     * List all active tools (for public display)
     */
    @GetMapping("/active")
    public ResponseEntity<List<ExternalTool>> listActiveTools() {
        log.info("GET /api/external-tools/active");
        List<ExternalTool> tools = externalToolService.listActiveTools();
        return ResponseEntity.ok(tools);
    }

    /**
     * List all tools with pagination (for admin)
     */
    @GetMapping
    public ResponseEntity<IPage<ExternalTool>> listTools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/external-tools - page: {}, size: {}", page, size);
        IPage<ExternalTool> tools = externalToolService.listTools(page, size);
        return ResponseEntity.ok(tools);
    }

    /**
     * List tools by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> listToolsByCategory(@PathVariable String category) {
        log.info("GET /api/external-tools/category/{}", category);
        try {
            ExternalTool.ToolCategory toolCategory = ExternalTool.ToolCategory.valueOf(category.toUpperCase());
            List<ExternalTool> tools = externalToolService.listToolsByCategory(toolCategory);
            return ResponseEntity.ok(tools);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid category: " + category));
        }
    }

    /**
     * Search tools
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExternalTool>> searchTools(@RequestParam String keyword) {
        log.info("GET /api/external-tools/search - keyword: {}", keyword);
        List<ExternalTool> tools = externalToolService.searchTools(keyword);
        return ResponseEntity.ok(tools);
    }

    /**
     * Update a tool
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTool(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/external-tools/{}", id);
        
        ExternalTool.ToolCategory category = null;
        if (request.get("category") != null) {
            try {
                category = ExternalTool.ToolCategory.valueOf((String) request.get("category"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid category: " + request.get("category")));
            }
        }
        
        Integer displayOrder = null;
        Object displayOrderObj = request.get("displayOrder");
        if (displayOrderObj != null) {
            if (displayOrderObj instanceof Number) {
                displayOrder = ((Number) displayOrderObj).intValue();
            }
        }
        
        Boolean isActive = null;
        Object isActiveObj = request.get("isActive");
        if (isActiveObj != null) {
            if (isActiveObj instanceof Boolean) {
                isActive = (Boolean) isActiveObj;
            }
        }
        
        ExternalTool tool = externalToolService.updateTool(
                id,
                (String) request.get("name"),
                (String) request.get("description"),
                (String) request.get("url"),
                (String) request.get("icon"),
                (String) request.get("iconBgColor"),
                category,
                displayOrder,
                isActive
        );
        return ResponseEntity.ok(tool);
    }

    /**
     * Delete a tool
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTool(@PathVariable Long id) {
        log.info("DELETE /api/external-tools/{}", id);
        externalToolService.deleteTool(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Initialize default tools (admin only)
     */
    @PostMapping("/init")
    public ResponseEntity<?> initializeTools() {
        log.info("POST /api/external-tools/init - Initializing default tools");
        externalToolService.initializeDefaultTools();
        return ResponseEntity.ok(Map.of("message", "Default tools initialized"));
    }
}

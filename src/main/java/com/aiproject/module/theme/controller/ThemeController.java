package com.aiproject.module.theme.controller;

import com.aiproject.module.theme.model.ThemeConfig;
import com.aiproject.module.theme.model.ThemeRequest;
import com.aiproject.module.theme.model.ThemeResponse;
import com.aiproject.module.theme.service.ThemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Theme Controller
 * REST API endpoints for theme management
 */
@Slf4j
@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    /**
     * Get all themes
     */
    @GetMapping
    public ResponseEntity<List<ThemeResponse>> listThemes() {
        log.info("GET /api/themes - List all themes");
        List<ThemeResponse> themes = themeService.listThemes();
        return ResponseEntity.ok(themes);
    }

    /**
     * Get theme by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> getTheme(@PathVariable Long id) {
        log.info("GET /api/themes/{}", id);
        ThemeResponse theme = themeService.getThemeById(id);
        return ResponseEntity.ok(theme);
    }

    /**
     * Get theme by theme ID (folder name)
     */
    @GetMapping("/by-theme-id/{themeId}")
    public ResponseEntity<ThemeResponse> getThemeByThemeId(@PathVariable String themeId) {
        log.info("GET /api/themes/by-theme-id/{}", themeId);
        ThemeResponse theme = themeService.getThemeByThemeId(themeId);
        return ResponseEntity.ok(theme);
    }

    /**
     * Get currently active theme
     */
    @GetMapping("/active")
    public ResponseEntity<ThemeResponse> getActiveTheme() {
        log.info("GET /api/themes/active");
        ThemeResponse theme = themeService.getActiveTheme();
        return ResponseEntity.ok(theme);
    }

    /**
     * Activate a theme
     */
    @PostMapping("/{themeId}/activate")
    public ResponseEntity<ThemeResponse> activateTheme(@PathVariable String themeId) {
        log.info("POST /api/themes/{}/activate", themeId);
        ThemeResponse theme = themeService.activateTheme(themeId);
        return ResponseEntity.ok(theme);
    }

    /**
     * Enable a theme
     */
    @PostMapping("/{themeId}/enable")
    public ResponseEntity<ThemeResponse> enableTheme(@PathVariable String themeId) {
        log.info("POST /api/themes/{}/enable", themeId);
        ThemeResponse theme = themeService.enableTheme(themeId);
        return ResponseEntity.ok(theme);
    }

    /**
     * Disable a theme
     */
    @PostMapping("/{themeId}/disable")
    public ResponseEntity<ThemeResponse> disableTheme(@PathVariable String themeId) {
        log.info("POST /api/themes/{}/disable", themeId);
        ThemeResponse theme = themeService.disableTheme(themeId);
        return ResponseEntity.ok(theme);
    }

    /**
     * Update theme settings
     */
    @PutMapping("/{themeId}/settings")
    public ResponseEntity<ThemeResponse> updateThemeSettings(
            @PathVariable String themeId,
            @RequestBody ThemeRequest request) {
        log.info("PUT /api/themes/{}/settings", themeId);
        ThemeResponse theme = themeService.updateThemeSettings(themeId, request.getSettings());
        return ResponseEntity.ok(theme);
    }

    /**
     * Get theme settings schema
     */
    @GetMapping("/{themeId}/settings/schema")
    public ResponseEntity<ThemeConfig> getThemeSettingsSchema(@PathVariable String themeId) {
        log.info("GET /api/themes/{}/settings/schema", themeId);
        ThemeConfig config = themeService.getThemeSettingsSchema(themeId);
        return ResponseEntity.ok(config);
    }

    /**
     * Install theme from ZIP file
     */
    @PostMapping("/install")
    public ResponseEntity<ThemeResponse> installTheme(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/themes/install - {}", file.getOriginalFilename());
        ThemeResponse theme = themeService.installTheme(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(theme);
    }

    /**
     * Delete a theme
     */
    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable String themeId) {
        log.info("DELETE /api/themes/{}", themeId);
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get theme screenshot
     */
    @GetMapping("/{themeId}/screenshot")
    public ResponseEntity<Resource> getThemeScreenshot(@PathVariable String themeId) {
        try {
            ThemeResponse theme = themeService.getThemeByThemeId(themeId);
            if (theme.getScreenshot() == null || theme.getScreenshot().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Path screenshotPath = themeService.getStaticPath(themeId)
                .getParent()
                .resolve(theme.getScreenshot());
            
            if (!Files.exists(screenshotPath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(screenshotPath);
            String contentType = Files.probeContentType(screenshotPath);
            if (contentType == null) {
                contentType = "image/png";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
        } catch (Exception e) {
            log.error("Failed to get screenshot for theme: {}", themeId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Preview theme (get context for preview)
     */
    @GetMapping("/{themeId}/preview")
    public ResponseEntity<Map<String, Object>> previewTheme(@PathVariable String themeId) {
        log.info("GET /api/themes/{}/preview", themeId);
        ThemeResponse theme = themeService.getThemeByThemeId(themeId);
        
        Map<String, Object> preview = Map.of(
            "theme", theme,
            "previewUrl", "/themes/" + themeId + "/preview"
        );
        
        return ResponseEntity.ok(preview);
    }
}

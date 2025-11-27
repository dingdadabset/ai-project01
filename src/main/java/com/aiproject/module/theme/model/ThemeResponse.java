package com.aiproject.module.theme.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Theme Response DTO
 * For API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponse {

    private Long id;
    
    private String themeId;
    
    private String name;
    
    private String version;
    
    private String author;
    
    private String authorUrl;
    
    private String description;
    
    private String screenshot;
    
    private Boolean isActive;
    
    private Theme.ThemeStatus status;
    
    private ThemeConfig config;
    
    private Map<String, Object> settings;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

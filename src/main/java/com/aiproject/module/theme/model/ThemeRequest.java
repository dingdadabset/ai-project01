package com.aiproject.module.theme.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Theme Request DTO
 * For theme settings update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeRequest {

    /**
     * Theme settings to update
     */
    private Map<String, Object> settings;
}

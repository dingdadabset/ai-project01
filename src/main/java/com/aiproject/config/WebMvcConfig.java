package com.aiproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC Configuration
 * Configures static resource handlers for themes and uploads
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${theme.directory:themes}")
    private String themeDirectory;

    @Value("${upload.directory:uploads}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve theme static resources
        String themesPath = Paths.get(System.getProperty("user.dir"), themeDirectory).toUri().toString();
        
        registry.addResourceHandler("/themes/**")
                .addResourceLocations(themesPath)
                .setCachePeriod(3600);

        // Serve uploaded files
        String uploadsPath = Paths.get(System.getProperty("user.dir"), uploadDirectory).toUri().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsPath)
                .setCachePeriod(3600);
    }
}

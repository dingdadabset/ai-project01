package com.aiproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC Configuration
 * Configures static resource handlers for themes
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${theme.directory:themes}")
    private String themeDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve theme static resources
        String themesPath = Paths.get(System.getProperty("user.dir"), themeDirectory).toUri().toString();
        
        registry.addResourceHandler("/themes/**")
                .addResourceLocations(themesPath)
                .setCachePeriod(3600);
    }
}

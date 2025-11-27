package com.aiproject.config;

import com.aiproject.module.theme.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * Thymeleaf Configuration
 * Configures template engine for theme rendering
 */
@Configuration
@RequiredArgsConstructor
public class ThymeleafConfig {

    private final TranslationService translationService;

    /**
     * Configure FileTemplateResolver for theme templates
     */
    @Bean
    public FileTemplateResolver themeTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(System.getProperty("user.dir") + "/themes/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        resolver.setOrder(2);
        resolver.setCheckExistence(true);
        return resolver;
    }
}

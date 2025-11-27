package com.aiproject.module.theme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Translation Service
 * Provides internationalization support for themes
 */
@Slf4j
@Service
public class TranslationService {

    @Value("${theme.directory:themes}")
    private String themeDirectory;

    @Value("${i18n.default-locale:en}")
    private String defaultLocale;

    /**
     * Cache for translations: themeId -> locale -> key -> value
     */
    private final Map<String, Map<String, Map<String, String>>> translationCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing translation service with default locale: {}", defaultLocale);
    }

    /**
     * Get translated text for a key
     *
     * @param themeId Theme identifier
     * @param locale  Locale code (e.g., "en", "zh-CN")
     * @param key     Translation key
     * @return Translated text or the key if not found
     */
    public String translate(String themeId, String locale, String key) {
        Map<String, String> translations = getTranslations(themeId, locale);
        
        // Try exact locale
        if (translations.containsKey(key)) {
            return translations.get(key);
        }
        
        // Try base locale (e.g., "zh" for "zh-CN")
        if (locale.contains("-")) {
            String baseLocale = locale.split("-")[0];
            translations = getTranslations(themeId, baseLocale);
            if (translations.containsKey(key)) {
                return translations.get(key);
            }
        }
        
        // Fallback to default locale
        if (!locale.equals(defaultLocale)) {
            translations = getTranslations(themeId, defaultLocale);
            if (translations.containsKey(key)) {
                return translations.get(key);
            }
        }
        
        // Return key as fallback
        return key;
    }

    /**
     * Get translated text with placeholder substitution
     *
     * @param themeId Theme identifier
     * @param locale  Locale code
     * @param key     Translation key
     * @param args    Arguments for placeholder substitution
     * @return Translated text with placeholders replaced
     */
    public String translate(String themeId, String locale, String key, Object... args) {
        String template = translate(themeId, locale, key);
        return String.format(template, args);
    }

    /**
     * Get all translations for a theme and locale
     *
     * @param themeId Theme identifier
     * @param locale  Locale code
     * @return Map of translation key to value
     */
    public Map<String, String> getTranslations(String themeId, String locale) {
        return translationCache
            .computeIfAbsent(themeId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(locale, k -> loadTranslations(themeId, locale));
    }

    /**
     * Get all available locales for a theme
     *
     * @param themeId Theme identifier
     * @return List of available locale codes
     */
    public List<String> getAvailableLocales(String themeId) {
        Path i18nPath = getI18nPath(themeId);
        if (!Files.exists(i18nPath)) {
            return List.of(defaultLocale);
        }

        List<String> locales = new ArrayList<>();
        try (var stream = Files.list(i18nPath)) {
            stream.filter(p -> p.toString().endsWith(".properties"))
                .forEach(p -> {
                    String filename = p.getFileName().toString();
                    if (filename.startsWith("messages_")) {
                        String locale = filename
                            .replace("messages_", "")
                            .replace(".properties", "");
                        locales.add(locale);
                    } else if (filename.equals("messages.properties")) {
                        locales.add(defaultLocale);
                    }
                });
        } catch (IOException e) {
            log.warn("Failed to list locales for theme: {}", themeId, e);
        }

        return locales.isEmpty() ? List.of(defaultLocale) : locales;
    }

    /**
     * Clear translation cache for a theme
     *
     * @param themeId Theme identifier
     */
    public void clearCache(String themeId) {
        translationCache.remove(themeId);
        log.info("Cleared translation cache for theme: {}", themeId);
    }

    /**
     * Clear all translation caches
     */
    public void clearAllCaches() {
        translationCache.clear();
        log.info("Cleared all translation caches");
    }

    /**
     * Reload translations for a theme
     *
     * @param themeId Theme identifier
     */
    public void reloadTranslations(String themeId) {
        clearCache(themeId);
        // Translations will be reloaded on next access
    }

    // ==================== Private Helper Methods ====================

    private Path getThemesPath() {
        return Paths.get(System.getProperty("user.dir"), themeDirectory);
    }

    private Path getI18nPath(String themeId) {
        return getThemesPath().resolve(themeId).resolve("i18n");
    }

    private Map<String, String> loadTranslations(String themeId, String locale) {
        Map<String, String> translations = new HashMap<>();
        
        Path i18nPath = getI18nPath(themeId);
        if (!Files.exists(i18nPath)) {
            log.debug("No i18n directory found for theme: {}", themeId);
            return translations;
        }

        // Try specific locale file
        Path localeFile = i18nPath.resolve("messages_" + locale + ".properties");
        if (!Files.exists(localeFile)) {
            // Try underscore format (e.g., zh_CN instead of zh-CN)
            localeFile = i18nPath.resolve("messages_" + locale.replace("-", "_") + ".properties");
        }
        if (!Files.exists(localeFile)) {
            // Try default messages file
            localeFile = i18nPath.resolve("messages.properties");
        }

        if (Files.exists(localeFile)) {
            try (InputStream is = Files.newInputStream(localeFile);
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                props.forEach((key, value) -> translations.put(key.toString(), value.toString()));
                log.debug("Loaded {} translations for theme {} locale {}", translations.size(), themeId, locale);
            } catch (IOException e) {
                log.warn("Failed to load translations for theme {} locale {}", themeId, locale, e);
            }
        }

        return translations;
    }
}

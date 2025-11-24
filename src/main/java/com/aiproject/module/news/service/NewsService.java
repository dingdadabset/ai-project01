package com.aiproject.module.news.service;

import com.aiproject.module.news.model.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * News Service
 * Handles business logic for fetching and managing news articles
 * Inspired by Halo's service layer pattern
 */
@Slf4j
@Service
public class NewsService {

    // Static mock data for consistent and efficient responses
    private static final List<NewsArticle> MOCK_ARTICLES = initializeMockArticles();

    private static List<NewsArticle> initializeMockArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        
        articles.add(NewsArticle.builder()
                .id("news-001")
                .title("AI Breakthrough: New Model Surpasses Human Performance")
                .description("Researchers announce a new AI model that achieves unprecedented results")
                .content("In a groundbreaking development, researchers have unveiled a new AI model...")
                .source("TechCrunch")
                .author("Jane Smith")
                .url("https://example.com/ai-breakthrough")
                .imageUrl("https://example.com/images/ai.jpg")
                .publishedAt(LocalDateTime.now().minusHours(2))
                .category("technology")
                .viewCount(15000L)
                .isHot(true)
                .build());

        articles.add(NewsArticle.builder()
                .id("news-002")
                .title("Stock Markets Hit Record Highs Amid Economic Recovery")
                .description("Global stock markets surge as economic indicators show strong recovery")
                .content("Stock markets around the world reached record highs today...")
                .source("Bloomberg")
                .author("John Doe")
                .url("https://example.com/market-highs")
                .imageUrl("https://example.com/images/market.jpg")
                .publishedAt(LocalDateTime.now().minusHours(1))
                .category("business")
                .viewCount(12000L)
                .isHot(true)
                .build());

        articles.add(NewsArticle.builder()
                .id("news-003")
                .title("Climate Summit Reaches Historic Agreement")
                .description("World leaders agree on ambitious climate action plan")
                .content("In a historic moment, world leaders have reached a comprehensive agreement...")
                .source("Reuters")
                .author("Alice Johnson")
                .url("https://example.com/climate-summit")
                .imageUrl("https://example.com/images/climate.jpg")
                .publishedAt(LocalDateTime.now().minusHours(3))
                .category("world")
                .viewCount(18000L)
                .isHot(true)
                .build());

        articles.add(NewsArticle.builder()
                .id("news-004")
                .title("New Programming Language Gains Popularity")
                .description("Developers embrace new language for its simplicity and performance")
                .content("A new programming language is rapidly gaining traction among developers...")
                .source("Dev.to")
                .author("Bob Wilson")
                .url("https://example.com/new-language")
                .imageUrl("https://example.com/images/code.jpg")
                .publishedAt(LocalDateTime.now().minusHours(5))
                .category("technology")
                .viewCount(8000L)
                .isHot(false)
                .build());

        articles.add(NewsArticle.builder()
                .id("news-005")
                .title("Sports Team Wins Championship After Dramatic Final")
                .description("Underdog team claims victory in thrilling championship match")
                .content("In an unforgettable championship final, the underdog team emerged victorious...")
                .source("ESPN")
                .author("Mike Brown")
                .url("https://example.com/championship")
                .imageUrl("https://example.com/images/sports.jpg")
                .publishedAt(LocalDateTime.now().minusHours(4))
                .category("sports")
                .viewCount(20000L)
                .isHot(true)
                .build());

        return articles;
    }

    /**
     * Get hot/trending news articles
     * In a real implementation, this would fetch from external APIs like NewsAPI, RSS feeds, etc.
     * 
     * @param limit Maximum number of articles to return
     * @return List of hot news articles
     */
    @Cacheable(value = "hotNews", unless = "#result == null")
    public List<NewsArticle> getHotNews(int limit) {
        log.info("Fetching hot news with limit: {}", limit);
        
        // Use static mock data
        return MOCK_ARTICLES.stream()
                .filter(article -> article.getIsHot() != null && article.getIsHot())
                .sorted(Comparator.comparing(NewsArticle::getViewCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get news articles by category
     * 
     * @param category Category to filter by
     * @param limit Maximum number of articles to return
     * @return List of news articles in the specified category
     */
    @Cacheable(value = "newsByCategory", key = "#category + '_' + #limit")
    public List<NewsArticle> getNewsByCategory(String category, int limit) {
        log.info("Fetching news for category: {} with limit: {}", category, limit);
        
        return MOCK_ARTICLES.stream()
                .filter(article -> category.equalsIgnoreCase(article.getCategory()))
                .sorted(Comparator.comparing(NewsArticle::getPublishedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get all news articles
     * 
     * @param limit Maximum number of articles to return
     * @return List of all news articles
     */
    public List<NewsArticle> getAllNews(int limit) {
        log.info("Fetching all news with limit: {}", limit);
        
        return MOCK_ARTICLES.stream()
                .sorted(Comparator.comparing(NewsArticle::getPublishedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}

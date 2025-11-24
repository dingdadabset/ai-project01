package com.aiproject.module.news.controller;

import com.aiproject.module.news.model.NewsArticle;
import com.aiproject.module.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * News Controller
 * REST API endpoints for news functionality
 * Following Halo's RESTful API design pattern
 */
@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * Get hot/trending news
     * 
     * @param limit Optional limit parameter (default: 10)
     * @return List of hot news articles
     */
    @GetMapping("/hot")
    public ResponseEntity<List<NewsArticle>> getHotNews(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/news/hot - limit: {}", limit);
        List<NewsArticle> hotNews = newsService.getHotNews(limit);
        return ResponseEntity.ok(hotNews);
    }

    /**
     * Get news by category
     * 
     * @param category Category to filter by
     * @param limit Optional limit parameter (default: 10)
     * @return List of news articles in the category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<NewsArticle>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/news/category/{} - limit: {}", category, limit);
        List<NewsArticle> news = newsService.getNewsByCategory(category, limit);
        return ResponseEntity.ok(news);
    }

    /**
     * Get all news
     * 
     * @param limit Optional limit parameter (default: 20)
     * @return List of all news articles
     */
    @GetMapping
    public ResponseEntity<List<NewsArticle>> getAllNews(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("GET /api/news - limit: {}", limit);
        List<NewsArticle> news = newsService.getAllNews(limit);
        return ResponseEntity.ok(news);
    }
}

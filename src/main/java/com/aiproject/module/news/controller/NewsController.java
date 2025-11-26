package com.aiproject.module.news.controller;

import com.aiproject.module.news.model.News;
import com.aiproject.module.news.service.NewsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * News Controller
 * REST API endpoints for hot news
 */
@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * Create a new news item
     */
    @PostMapping
    public ResponseEntity<News> createNews(@RequestBody Map<String, Object> request) {
        log.info("POST /api/news - Creating new news");
        
        News.NewsCategory category = null;
        if (request.get("category") != null) {
            category = News.NewsCategory.valueOf((String) request.get("category"));
        }
        
        News news = newsService.createNews(
                (String) request.get("title"),
                (String) request.get("summary"),
                (String) request.get("content"),
                (String) request.get("source"),
                (String) request.get("sourceUrl"),
                (String) request.get("thumbnail"),
                category
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(news);
    }

    /**
     * Get news by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        log.info("GET /api/news/{}", id);
        News news = newsService.getNewsById(id);
        newsService.incrementViewCount(id);
        return ResponseEntity.ok(news);
    }

    /**
     * List all news with pagination
     */
    @GetMapping
    public ResponseEntity<IPage<News>> listNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/news - page: {}, size: {}", page, size);
        IPage<News> news = newsService.listNews(page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * List hot news
     */
    @GetMapping("/hot")
    public ResponseEntity<List<News>> listHotNews(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/news/hot - limit: {}", limit);
        List<News> news = newsService.listHotNews(limit);
        return ResponseEntity.ok(news);
    }

    /**
     * List news by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<IPage<News>> listNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/news/category/{}", category);
        News.NewsCategory newsCategory = News.NewsCategory.valueOf(category.toUpperCase());
        IPage<News> news = newsService.listNewsByCategory(newsCategory, page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Search news by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<IPage<News>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/news/search - keyword: {}", keyword);
        IPage<News> news = newsService.searchNews(keyword, page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Update a news item
     */
    @PutMapping("/{id}")
    public ResponseEntity<News> updateNews(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/news/{}", id);
        
        News.NewsCategory category = null;
        if (request.get("category") != null) {
            category = News.NewsCategory.valueOf((String) request.get("category"));
        }
        
        Boolean isHot = request.get("isHot") != null ? (Boolean) request.get("isHot") : null;
        Integer hotScore = request.get("hotScore") != null ? (Integer) request.get("hotScore") : null;
        
        News news = newsService.updateNews(
                id,
                (String) request.get("title"),
                (String) request.get("summary"),
                (String) request.get("content"),
                (String) request.get("source"),
                (String) request.get("sourceUrl"),
                (String) request.get("thumbnail"),
                category,
                isHot,
                hotScore
        );
        return ResponseEntity.ok(news);
    }

    /**
     * Delete a news item
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        log.info("DELETE /api/news/{}", id);
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set news as hot
     */
    @PutMapping("/{id}/hot")
    public ResponseEntity<News> setHot(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/news/{}/hot", id);
        boolean isHot = (Boolean) request.getOrDefault("isHot", true);
        int hotScore = (Integer) request.getOrDefault("hotScore", 0);
        News news = newsService.setHot(id, isHot, hotScore);
        return ResponseEntity.ok(news);
    }
}

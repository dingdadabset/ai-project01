package com.aiproject.module.news.service;

import com.aiproject.module.news.model.NewsArticle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for NewsService
 */
@SpringBootTest
class NewsServiceTest {

    @Autowired
    private NewsService newsService;

    @Test
    void testGetHotNews() {
        List<NewsArticle> hotNews = newsService.getHotNews(5);
        
        assertNotNull(hotNews);
        assertTrue(hotNews.size() <= 5);
        
        // Verify all returned articles are marked as hot
        for (NewsArticle article : hotNews) {
            assertTrue(article.getIsHot());
            assertNotNull(article.getTitle());
            assertNotNull(article.getSource());
        }
    }

    @Test
    void testGetNewsByCategory() {
        String category = "technology";
        List<NewsArticle> news = newsService.getNewsByCategory(category, 10);
        
        assertNotNull(news);
        
        // Verify all articles belong to the requested category
        for (NewsArticle article : news) {
            assertEquals(category, article.getCategory());
        }
    }

    @Test
    void testGetAllNews() {
        List<NewsArticle> allNews = newsService.getAllNews(20);
        
        assertNotNull(allNews);
        assertTrue(allNews.size() <= 20);
        
        // Verify articles have required fields
        for (NewsArticle article : allNews) {
            assertNotNull(article.getId());
            assertNotNull(article.getTitle());
            assertNotNull(article.getPublishedAt());
        }
    }
}

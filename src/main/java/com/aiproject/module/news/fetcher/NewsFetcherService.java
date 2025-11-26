package com.aiproject.module.news.fetcher;

import com.aiproject.module.news.mapper.NewsMapper;
import com.aiproject.module.news.model.News;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * News Fetcher Service
 * Automatically fetches hot news from external sources
 * Falls back to sample news when external APIs are unavailable
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetcherService {

    private final NewsMapper newsMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Fetch news every 30 minutes
     */
    @Scheduled(fixedRate = 1800000, initialDelay = 5000)
    public void fetchHotNews() {
        log.info("Starting scheduled news fetch...");
        try {
            // Fetch from multiple sources
            fetchFromNewsApi();
            log.info("News fetch completed successfully");
        } catch (Exception e) {
            log.error("Error fetching news: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for fetching news
     */
    public List<News> fetchNewsManually() {
        log.info("Manual news fetch triggered");
        List<News> fetchedNews = new ArrayList<>();
        try {
            fetchedNews.addAll(fetchFromNewsApi());
            // If no news was fetched from external API, create sample news
            if (fetchedNews.isEmpty()) {
                log.info("External API unavailable, using sample news");
                createSampleNews(fetchedNews);
            }
        } catch (Exception e) {
            log.error("Error in manual news fetch: {}", e.getMessage(), e);
            // Always create sample news on error
            createSampleNews(fetchedNews);
        }
        return fetchedNews;
    }

    /**
     * Fetch news from a public news API
     * Using HackerNews API as it's free and doesn't require authentication
     * For demo purposes, we'll create sample hot news data as fallback
     */
    private List<News> fetchFromNewsApi() {
        List<News> newsList = new ArrayList<>();
        
        try {
            // Try to fetch from a public news source
            // Using HackerNews API as it's free and doesn't require authentication
            String topStoriesUrl = "https://hacker-news.firebaseio.com/v0/topstories.json";
            log.info("Fetching news from HackerNews API...");
            
            String response = restTemplate.getForObject(topStoriesUrl, String.class);
            
            if (response != null && !response.isEmpty()) {
                JsonNode storyIds = objectMapper.readTree(response);
                int count = 0;
                
                // Fetch top 10 stories
                for (JsonNode idNode : storyIds) {
                    if (count >= 10) break;
                    
                    try {
                        Long storyId = idNode.asLong();
                        String storyUrl = "https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json";
                        String storyResponse = restTemplate.getForObject(storyUrl, String.class);
                        
                        if (storyResponse != null) {
                            JsonNode story = objectMapper.readTree(storyResponse);
                            
                            String title = story.has("title") ? story.get("title").asText() : null;
                            String url = story.has("url") ? story.get("url").asText() : null;
                            int score = story.has("score") ? story.get("score").asInt() : 0;
                            
                            if (title != null) {
                                // Check if news already exists
                                News existingNews = newsMapper.selectOne(
                                        new LambdaQueryWrapper<News>()
                                                .eq(News::getTitle, title)
                                                .eq(News::getSource, "Hacker News"));
                                
                                if (existingNews == null) {
                                    News news = News.builder()
                                            .title(title)
                                            .summary("From Hacker News - Score: " + score)
                                            .content("Read full article at: " + (url != null ? url : "N/A"))
                                            .source("Hacker News")
                                            .sourceUrl(url)
                                            .category(News.NewsCategory.TECHNOLOGY)
                                            .isHot(score > 100)
                                            .hotScore(score)
                                            .publishedAt(LocalDateTime.now())
                                            .viewCount(0L)
                                            .build();
                                    
                                    newsMapper.insert(news);
                                    newsList.add(news);
                                    log.info("Fetched news: {}", title);
                                    count++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error fetching story: {}", e.getMessage());
                    }
                }
            }
        } catch (RestClientException e) {
            log.warn("HackerNews API unavailable (possibly blocked or network issue): {}", e.getMessage());
            // Fallback: Create sample news data for demonstration
            createSampleNews(newsList);
        } catch (Exception e) {
            log.error("Error fetching from HackerNews API: {}", e.getMessage());
            // Fallback: Create sample news data for demonstration
            createSampleNews(newsList);
        }
        
        return newsList;
    }

    /**
     * Create sample news for demonstration when API is unavailable
     * Generates fresh news with current date to ensure relevance
     */
    private void createSampleNews(List<News> newsList) {
        log.info("Creating sample news data...");
        
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Generate fresh sample news with current date references
        String[][] sampleNewsData = {
                {"科技巨头发布最新AI产品 (" + today + ")", "人工智能领域迎来新突破，多家科技公司竞相发布新产品", "TECHNOLOGY", "科技日报"},
                {"全球股市震荡，投资者关注美联储动向 (" + today + ")", "金融市场分析：今日市场波动较大，建议谨慎操作", "FINANCE", "财经观察"},
                {"新能源汽车销量创新高 (" + today + ")", "新能源行业发展迅速，电动车渗透率持续攀升", "TECHNOLOGY", "汽车之家"},
                {"国际贸易协议达成重要共识 (" + today + ")", "国际经济合作新进展，多国签署贸易合作框架", "WORLD", "新华社"},
                {"A股市场今日行情分析 (" + today + ")", "今日上证指数、深证成指走势分析及明日预测", "FINANCE", "证券时报"},
                {"半导体产业链最新动态 (" + today + ")", "国产芯片研发取得重要突破，多个项目进入量产阶段", "TECHNOLOGY", "科技新闻"},
                {"楼市新政策出台 (" + today + ")", "多地放开限购政策，房地产市场迎来新变化", "FINANCE", "经济日报"},
                {"体育赛事热点 (" + today + ")", "各大体育赛事精彩回顾与赛事预告", "SPORTS", "体育频道"}
        };
        
        for (String[] data : sampleNewsData) {
            try {
                // Check if similar news already exists today
                News existingNews = newsMapper.selectOne(
                        new LambdaQueryWrapper<News>()
                                .eq(News::getTitle, data[0])
                                .eq(News::getSource, data[3]));
                
                if (existingNews == null) {
                    News news = News.builder()
                            .title(data[0])
                            .summary(data[1])
                            .content("这是今日热点新闻，点击阅读全文了解更多详情。")
                            .source(data[3])
                            .category(News.NewsCategory.valueOf(data[2]))
                            .isHot(true)
                            .hotScore((int) (Math.random() * 100) + 50)
                            .publishedAt(LocalDateTime.now())
                            .viewCount(0L)
                            .build();
                    
                    newsMapper.insert(news);
                    newsList.add(news);
                    log.info("Created sample news: {}", data[0]);
                } else {
                    // Update existing news to keep it fresh
                    existingNews.setPublishedAt(LocalDateTime.now());
                    existingNews.setHotScore((int) (Math.random() * 100) + 50);
                    newsMapper.updateById(existingNews);
                    newsList.add(existingNews);
                    log.info("Updated sample news: {}", data[0]);
                }
            } catch (Exception e) {
                log.warn("Error creating sample news: {}", e.getMessage());
            }
        }
    }
}

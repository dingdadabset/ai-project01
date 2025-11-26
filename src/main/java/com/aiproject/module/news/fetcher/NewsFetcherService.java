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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * News Fetcher Service
 * Automatically fetches hot news from external sources
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
        } catch (Exception e) {
            log.error("Error in manual news fetch: {}", e.getMessage(), e);
        }
        return fetchedNews;
    }

    /**
     * Fetch news from a public news API
     * Using NewsAPI.org format - in production, you would need an API key
     * For demo purposes, we'll create sample hot news data
     */
    private List<News> fetchFromNewsApi() {
        List<News> newsList = new ArrayList<>();
        
        try {
            // Try to fetch from a public news source
            // Using HackerNews API as it's free and doesn't require authentication
            String topStoriesUrl = "https://hacker-news.firebaseio.com/v0/topstories.json";
            String response = restTemplate.getForObject(topStoriesUrl, String.class);
            
            if (response != null) {
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
        } catch (Exception e) {
            log.error("Error fetching from HackerNews API: {}", e.getMessage());
            // Fallback: Create sample news data for demonstration
            createSampleNews(newsList);
        }
        
        return newsList;
    }

    /**
     * Create sample news for demonstration when API is unavailable
     */
    private void createSampleNews(List<News> newsList) {
        log.info("Creating sample news data...");
        
        String[][] sampleNewsData = {
                {"科技巨头发布最新AI产品", "人工智能领域迎来新突破", "TECHNOLOGY"},
                {"全球股市震荡，投资者关注美联储动向", "金融市场分析", "FINANCE"},
                {"世界杯预选赛激战正酣", "体育赛事热点报道", "SPORTS"},
                {"新能源汽车销量创新高", "新能源行业发展迅速", "TECHNOLOGY"},
                {"国际贸易协议达成重要共识", "国际经济合作新进展", "WORLD"}
        };
        
        for (String[] data : sampleNewsData) {
            // Check if already exists
            News existingNews = newsMapper.selectOne(
                    new LambdaQueryWrapper<News>()
                            .eq(News::getTitle, data[0])
                            .eq(News::getSource, "系统生成"));
            
            if (existingNews == null) {
                News news = News.builder()
                        .title(data[0])
                        .summary(data[1])
                        .content("这是一条自动生成的示例新闻，用于演示系统功能。")
                        .source("系统生成")
                        .category(News.NewsCategory.valueOf(data[2]))
                        .isHot(true)
                        .hotScore((int) (Math.random() * 100) + 50)
                        .publishedAt(LocalDateTime.now())
                        .viewCount(0L)
                        .build();
                
                newsMapper.insert(news);
                newsList.add(news);
                log.info("Created sample news: {}", data[0]);
            }
        }
    }
}

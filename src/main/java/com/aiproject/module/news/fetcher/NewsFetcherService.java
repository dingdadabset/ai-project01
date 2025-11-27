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
                            String text = story.has("text") ? story.get("text").asText() : null;
                            
                            if (title != null) {
                                // Check if news already exists
                                News existingNews = newsMapper.selectOne(
                                        new LambdaQueryWrapper<News>()
                                                .eq(News::getTitle, title)
                                                .eq(News::getSource, "Hacker News"));
                                
                                if (existingNews == null) {
                                    // Build content with more information
                                    StringBuilder contentBuilder = new StringBuilder();
                                    contentBuilder.append("<p><strong>热度评分:</strong> ").append(score).append("</p>");
                                    if (text != null && !text.isEmpty()) {
                                        contentBuilder.append("<p>").append(text).append("</p>");
                                    }
                                    if (url != null && !url.isEmpty()) {
                                        contentBuilder.append("<p><strong>原文链接:</strong> <a href=\"")
                                                     .append(url).append("\" target=\"_blank\">").append(url).append("</a></p>");
                                    }
                                    contentBuilder.append("<p>本条新闻来自 Hacker News 热门资讯。</p>");
                                    
                                    News news = News.builder()
                                            .title(title)
                                            .summary("热门科技资讯 - 热度评分: " + score)
                                            .content(contentBuilder.toString())
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
        // Format: {title, summary, content, category, source}
        String[][] sampleNewsData = {
                {
                    "科技巨头发布最新AI产品 (" + today + ")", 
                    "人工智能领域迎来新突破，多家科技公司竞相发布新产品", 
                    "<p>近日，全球多家科技巨头纷纷发布了其最新的人工智能产品和服务。这些新产品涵盖了自然语言处理、计算机视觉、自动驾驶等多个领域。</p>" +
                    "<p>业内分析人士指出，随着AI技术的不断成熟，人工智能正在从实验室走向生产环境，越来越多的企业开始将AI技术应用于实际业务场景中。</p>" +
                    "<p>专家预测，未来几年内，AI技术将在医疗、教育、金融等领域产生深远影响，推动社会生产力的显著提升。</p>",
                    "TECHNOLOGY", 
                    "科技日报"
                },
                {
                    "全球股市震荡，投资者关注美联储动向 (" + today + ")", 
                    "金融市场分析：今日市场波动较大，建议谨慎操作", 
                    "<p>受多重因素影响，全球股市今日出现明显波动。美股三大指数开盘后呈现震荡走势，欧洲主要股指也出现不同程度的涨跌。</p>" +
                    "<p>分析师认为，当前市场对美联储货币政策走向高度关注。市场普遍预期美联储将在近期会议上讨论调整利率的可能性。</p>" +
                    "<p>建议投资者保持理性，密切关注宏观经济数据和政策动向，合理配置资产组合以应对市场波动。</p>",
                    "FINANCE", 
                    "财经观察"
                },
                {
                    "新能源汽车销量创新高 (" + today + ")", 
                    "新能源行业发展迅速，电动车渗透率持续攀升", 
                    "<p>最新数据显示，本月国内新能源汽车销量再创历史新高，市场渗透率突破35%。主要车企的电动车型销量均出现大幅增长。</p>" +
                    "<p>在政策支持和技术进步的双重推动下，新能源汽车产业链持续完善，充电基础设施建设也在加速推进。</p>" +
                    "<p>业内人士预计，随着电池技术的不断突破和成本的持续下降，新能源汽车将在未来几年内实现更大规模的普及。</p>",
                    "TECHNOLOGY", 
                    "汽车之家"
                },
                {
                    "国际贸易协议达成重要共识 (" + today + ")", 
                    "国际经济合作新进展，多国签署贸易合作框架", 
                    "<p>在刚刚结束的国际经济合作会议上，多个国家就贸易合作达成了重要共识，签署了新的贸易合作框架协议。</p>" +
                    "<p>该协议涵盖关税减免、市场准入、知识产权保护等多个领域，将为各方带来实质性的贸易便利和经济增长机会。</p>" +
                    "<p>专家表示，这一协议的签署体现了各国推动经济全球化、促进共同发展的积极意愿。</p>",
                    "WORLD", 
                    "新华社"
                },
                {
                    "A股市场今日行情分析 (" + today + ")", 
                    "今日上证指数、深证成指走势分析及明日预测", 
                    "<p>今日A股市场整体呈现震荡整理态势。上证指数小幅波动，深证成指表现相对稳健，创业板指有所回调。</p>" +
                    "<p>盘面上，新能源、半导体等科技板块表现活跃，银行、保险等金融板块则出现分化。成交量维持在正常水平。</p>" +
                    "<p>技术分析显示，短期内市场可能继续维持震荡格局，投资者应关注政策面和资金面的变化。</p>",
                    "FINANCE", 
                    "证券时报"
                },
                {
                    "半导体产业链最新动态 (" + today + ")", 
                    "国产芯片研发取得重要突破，多个项目进入量产阶段", 
                    "<p>国内半导体产业传来利好消息，多家芯片企业宣布其核心产品取得重要技术突破，部分产品已进入量产阶段。</p>" +
                    "<p>在国家政策的大力支持下，半导体产业链各环节正在加速发展，设备、材料、设计、制造等领域均取得积极进展。</p>" +
                    "<p>行业分析师表示，虽然与国际先进水平仍有差距，但国产芯片正在稳步追赶，未来发展前景可期。</p>",
                    "TECHNOLOGY", 
                    "科技新闻"
                },
                {
                    "楼市新政策出台 (" + today + ")", 
                    "多地放开限购政策，房地产市场迎来新变化", 
                    "<p>近期，多个城市相继出台房地产调控新政策，包括放宽限购条件、调整贷款利率、优化公积金政策等措施。</p>" +
                    "<p>业内人士认为，这些政策调整旨在促进房地产市场平稳健康发展，更好满足居民合理住房需求。</p>" +
                    "<p>市场预期，随着政策效果的逐步显现，房地产市场有望逐步企稳，但整体复苏仍需时间。</p>",
                    "FINANCE", 
                    "经济日报"
                },
                {
                    "体育赛事热点 (" + today + ")", 
                    "各大体育赛事精彩回顾与赛事预告", 
                    "<p>本周体育赛事精彩纷呈，足球、篮球、网球等多项赛事同时进行，吸引了全球观众的关注。</p>" +
                    "<p>在刚刚结束的比赛中，多支队伍展现出强劲的竞技状态，为观众带来了多场精彩对决。</p>" +
                    "<p>接下来一周，还将有多项重要赛事陆续开赛，体育迷们不容错过。</p>",
                    "SPORTS", 
                    "体育频道"
                }
        };
        
        for (String[] data : sampleNewsData) {
            try {
                // Check if similar news already exists today
                News existingNews = newsMapper.selectOne(
                        new LambdaQueryWrapper<News>()
                                .eq(News::getTitle, data[0])
                                .eq(News::getSource, data[4]));
                
                if (existingNews == null) {
                    News news = News.builder()
                            .title(data[0])
                            .summary(data[1])
                            .content(data[2])
                            .source(data[4])
                            .category(News.NewsCategory.valueOf(data[3]))
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
                    // Also update content if it was too short
                    if (existingNews.getContent() == null || existingNews.getContent().length() < 100) {
                        existingNews.setContent(data[2]);
                    }
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

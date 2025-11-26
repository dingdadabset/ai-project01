package com.aiproject.module.news.service;

import com.aiproject.module.news.mapper.NewsMapper;
import com.aiproject.module.news.model.News;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * News Service
 * Handles hot news business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService extends ServiceImpl<NewsMapper, News> {

    private final NewsMapper newsMapper;

    /**
     * Create a new news item
     */
    @Transactional
    public News createNews(String title, String summary, String content, String source,
                          String sourceUrl, String thumbnail, News.NewsCategory category) {
        log.info("Creating news: {}", title);
        
        News news = News.builder()
                .title(title)
                .summary(summary)
                .content(content)
                .source(source)
                .sourceUrl(sourceUrl)
                .thumbnail(thumbnail)
                .category(category != null ? category : News.NewsCategory.OTHER)
                .publishedAt(LocalDateTime.now())
                .build();
        
        newsMapper.insert(news);
        return news;
    }

    /**
     * Get news by ID
     */
    public News getNewsById(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("News not found");
        }
        return news;
    }

    /**
     * List all news with pagination
     */
    public IPage<News> listNews(int page, int size) {
        Page<News> newsPage = new Page<>(page + 1, size);
        return newsMapper.selectPage(newsPage,
                new LambdaQueryWrapper<News>().orderByDesc(News::getPublishedAt));
    }

    /**
     * List hot news
     */
    public List<News> listHotNews(int limit) {
        return newsMapper.selectList(
                new LambdaQueryWrapper<News>()
                        .eq(News::getIsHot, true)
                        .orderByDesc(News::getHotScore)
                        .last("LIMIT " + limit));
    }

    /**
     * List news by category
     */
    public IPage<News> listNewsByCategory(News.NewsCategory category, int page, int size) {
        Page<News> newsPage = new Page<>(page + 1, size);
        return newsMapper.selectPage(newsPage,
                new LambdaQueryWrapper<News>()
                        .eq(News::getCategory, category)
                        .orderByDesc(News::getPublishedAt));
    }

    /**
     * Search news by keyword
     */
    public IPage<News> searchNews(String keyword, int page, int size) {
        Page<News> newsPage = new Page<>(page + 1, size);
        return newsMapper.selectPage(newsPage,
                new LambdaQueryWrapper<News>()
                        .like(News::getTitle, keyword)
                        .or()
                        .like(News::getSummary, keyword)
                        .orderByDesc(News::getPublishedAt));
    }

    /**
     * Update news
     */
    @Transactional
    public News updateNews(Long id, String title, String summary, String content,
                          String source, String sourceUrl, String thumbnail,
                          News.NewsCategory category, Boolean isHot, Integer hotScore) {
        News news = getNewsById(id);
        
        if (title != null) news.setTitle(title);
        if (summary != null) news.setSummary(summary);
        if (content != null) news.setContent(content);
        if (source != null) news.setSource(source);
        if (sourceUrl != null) news.setSourceUrl(sourceUrl);
        if (thumbnail != null) news.setThumbnail(thumbnail);
        if (category != null) news.setCategory(category);
        if (isHot != null) news.setIsHot(isHot);
        if (hotScore != null) news.setHotScore(hotScore);
        
        newsMapper.updateById(news);
        return news;
    }

    /**
     * Delete news
     */
    @Transactional
    public void deleteNews(Long id) {
        log.info("Deleting news: {}", id);
        News news = getNewsById(id);
        newsMapper.deleteById(news.getId());
    }

    /**
     * Increment view count
     */
    @Transactional
    public void incrementViewCount(Long id) {
        News news = getNewsById(id);
        news.setViewCount(news.getViewCount() + 1);
        newsMapper.updateById(news);
    }

    /**
     * Set news as hot
     */
    @Transactional
    public News setHot(Long id, boolean isHot, int hotScore) {
        News news = getNewsById(id);
        news.setIsHot(isHot);
        news.setHotScore(hotScore);
        newsMapper.updateById(news);
        return news;
    }
}

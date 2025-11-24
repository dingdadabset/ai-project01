package com.aiproject.module.news.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * News Article Model
 * Represents a news article with title, content, source, and metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {

    /**
     * Unique identifier for the news article
     */
    private String id;

    /**
     * Title of the news article
     */
    private String title;

    /**
     * Brief description or summary
     */
    private String description;

    /**
     * Full content of the article
     */
    private String content;

    /**
     * Source of the news (e.g., "Reuters", "BBC", "CNN")
     */
    private String source;

    /**
     * Author of the article
     */
    private String author;

    /**
     * URL to the original article
     */
    private String url;

    /**
     * URL to the article's image
     */
    private String imageUrl;

    /**
     * Publication date and time
     */
    private LocalDateTime publishedAt;

    /**
     * Category of the news (e.g., "technology", "business", "sports")
     */
    private String category;

    /**
     * View count (for trending/hot news)
     */
    private Long viewCount;

    /**
     * Whether this is a trending/hot article
     */
    private Boolean isHot;
}

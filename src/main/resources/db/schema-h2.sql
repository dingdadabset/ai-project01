-- Halo Blog Database Schema for H2
-- Compatible with H2 in MySQL mode

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    description VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    post_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Tags table
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE,
    post_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

-- Posts table
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    summary VARCHAR(500),
    content CLOB,
    original_content CLOB,
    thumbnail VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    author_id BIGINT NOT NULL,
    category_id BIGINT,
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    comment_count INT DEFAULT 0,
    top_priority INT DEFAULT 0,
    allow_comment BOOLEAN DEFAULT TRUE,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Post-Tags relationship table
CREATE TABLE IF NOT EXISTS post_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Comments table
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT,
    guest_name VARCHAR(50),
    guest_email VARCHAR(100),
    content CLOB NOT NULL,
    parent_id BIGINT,
    ip_address VARCHAR(100),
    user_agent VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES comments(id)
);

-- Pages table
CREATE TABLE IF NOT EXISTS pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    content CLOB,
    original_content CLOB,
    author_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users(id)
);

-- Attachments table
CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(500) NOT NULL,
    url VARCHAR(500) NOT NULL,
    media_type VARCHAR(100),
    suffix VARCHAR(10),
    size BIGINT NOT NULL,
    width INT DEFAULT 0,
    height INT DEFAULT 0,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_category_id ON posts(category_id);
CREATE INDEX IF NOT EXISTS idx_posts_published_at ON posts(published_at);
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_status ON comments(status);
CREATE INDEX IF NOT EXISTS idx_attachments_user_id ON attachments(user_id);
CREATE INDEX IF NOT EXISTS idx_attachments_type ON attachments(type);

-- News table for hot news
CREATE TABLE IF NOT EXISTS news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    content CLOB,
    source VARCHAR(100),
    source_url VARCHAR(500),
    thumbnail VARCHAR(500),
    category VARCHAR(20) NOT NULL,
    view_count BIGINT DEFAULT 0,
    is_hot BOOLEAN DEFAULT FALSE,
    hot_score INT DEFAULT 0,
    published_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Stocks table for stock market data
CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    name_cn VARCHAR(100),
    market VARCHAR(20) NOT NULL,
    price DECIMAL(20, 4),
    change_amount DECIMAL(20, 4),
    change_percent DECIMAL(10, 4),
    high DECIMAL(20, 4),
    low DECIMAL(20, 4),
    open DECIMAL(20, 4),
    prev_close DECIMAL(20, 4),
    volume BIGINT,
    market_cap DECIMAL(30, 2),
    pe_ratio DECIMAL(20, 4),
    is_hot BOOLEAN DEFAULT FALSE,
    hot_rank INT DEFAULT 0,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Create indexes for news and stocks
CREATE INDEX IF NOT EXISTS idx_news_category ON news(category);
CREATE INDEX IF NOT EXISTS idx_news_is_hot ON news(is_hot);
CREATE INDEX IF NOT EXISTS idx_news_published_at ON news(published_at);
CREATE INDEX IF NOT EXISTS idx_stocks_market ON stocks(market);
CREATE INDEX IF NOT EXISTS idx_stocks_is_hot ON stocks(is_hot);
CREATE INDEX IF NOT EXISTS idx_stocks_symbol ON stocks(symbol);

-- External Tools table for quick links to external websites
CREATE TABLE IF NOT EXISTS external_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    url VARCHAR(500) NOT NULL,
    icon VARCHAR(100),
    icon_bg_color VARCHAR(20),
    category VARCHAR(30) NOT NULL,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for external_tools
CREATE INDEX IF NOT EXISTS idx_external_tools_category ON external_tools(category);
CREATE INDEX IF NOT EXISTS idx_external_tools_is_active ON external_tools(is_active);
CREATE INDEX IF NOT EXISTS idx_external_tools_display_order ON external_tools(display_order);

-- Themes table for theme management
CREATE TABLE IF NOT EXISTS themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theme_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    version VARCHAR(20),
    author VARCHAR(100),
    author_url VARCHAR(500),
    description VARCHAR(1000),
    screenshot VARCHAR(500),
    config_json CLOB,
    settings_json CLOB,
    is_active BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    template_engine VARCHAR(20) DEFAULT 'thymeleaf',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for themes
CREATE INDEX IF NOT EXISTS idx_themes_theme_id ON themes(theme_id);
CREATE INDEX IF NOT EXISTS idx_themes_is_active ON themes(is_active);
CREATE INDEX IF NOT EXISTS idx_themes_status ON themes(status);

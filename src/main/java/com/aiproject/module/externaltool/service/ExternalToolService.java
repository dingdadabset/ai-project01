package com.aiproject.module.externaltool.service;

import com.aiproject.module.externaltool.mapper.ExternalToolMapper;
import com.aiproject.module.externaltool.model.ExternalTool;
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
 * External Tool Service
 * Handles external tool business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalToolService extends ServiceImpl<ExternalToolMapper, ExternalTool> {

    private final ExternalToolMapper externalToolMapper;

    /**
     * Create a new external tool
     */
    @Transactional
    public ExternalTool createTool(String name, String description, String url, 
                                   String icon, String iconBgColor, 
                                   ExternalTool.ToolCategory category, Integer displayOrder) {
        log.info("Creating external tool: {}", name);
        
        ExternalTool tool = ExternalTool.builder()
                .name(name)
                .description(description)
                .url(url)
                .icon(icon)
                .iconBgColor(iconBgColor)
                .category(category != null ? category : ExternalTool.ToolCategory.OTHER)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        externalToolMapper.insert(tool);
        return tool;
    }

    /**
     * Get tool by ID
     */
    public ExternalTool getToolById(Long id) {
        ExternalTool tool = externalToolMapper.selectById(id);
        if (tool == null) {
            throw new RuntimeException("External tool not found");
        }
        return tool;
    }

    /**
     * List all active tools ordered by displayOrder
     */
    public List<ExternalTool> listActiveTools() {
        return externalToolMapper.selectList(
                new LambdaQueryWrapper<ExternalTool>()
                        .eq(ExternalTool::getIsActive, true)
                        .orderByAsc(ExternalTool::getDisplayOrder)
                        .orderByAsc(ExternalTool::getId));
    }

    /**
     * List all tools with pagination
     */
    public IPage<ExternalTool> listTools(int page, int size) {
        Page<ExternalTool> toolPage = new Page<>(page + 1, size);
        return externalToolMapper.selectPage(toolPage,
                new LambdaQueryWrapper<ExternalTool>()
                        .orderByAsc(ExternalTool::getDisplayOrder)
                        .orderByAsc(ExternalTool::getId));
    }

    /**
     * List tools by category
     */
    public List<ExternalTool> listToolsByCategory(ExternalTool.ToolCategory category) {
        return externalToolMapper.selectList(
                new LambdaQueryWrapper<ExternalTool>()
                        .eq(ExternalTool::getCategory, category)
                        .eq(ExternalTool::getIsActive, true)
                        .orderByAsc(ExternalTool::getDisplayOrder));
    }

    /**
     * Update an external tool
     */
    @Transactional
    public ExternalTool updateTool(Long id, String name, String description, String url,
                                   String icon, String iconBgColor,
                                   ExternalTool.ToolCategory category, Integer displayOrder,
                                   Boolean isActive) {
        ExternalTool tool = getToolById(id);
        
        if (name != null) tool.setName(name);
        if (description != null) tool.setDescription(description);
        if (url != null) tool.setUrl(url);
        if (icon != null) tool.setIcon(icon);
        if (iconBgColor != null) tool.setIconBgColor(iconBgColor);
        if (category != null) tool.setCategory(category);
        if (displayOrder != null) tool.setDisplayOrder(displayOrder);
        if (isActive != null) tool.setIsActive(isActive);
        tool.setUpdatedAt(LocalDateTime.now());
        
        externalToolMapper.updateById(tool);
        return tool;
    }

    /**
     * Delete an external tool
     */
    @Transactional
    public void deleteTool(Long id) {
        log.info("Deleting external tool: {}", id);
        ExternalTool tool = getToolById(id);
        externalToolMapper.deleteById(tool.getId());
    }

    /**
     * Search tools by keyword
     */
    public List<ExternalTool> searchTools(String keyword) {
        return externalToolMapper.selectList(
                new LambdaQueryWrapper<ExternalTool>()
                        .eq(ExternalTool::getIsActive, true)
                        .and(wrapper -> wrapper
                                .like(ExternalTool::getName, keyword)
                                .or()
                                .like(ExternalTool::getDescription, keyword))
                        .orderByAsc(ExternalTool::getDisplayOrder));
    }

    /**
     * Initialize default tools if none exist
     */
    @Transactional
    public void initializeDefaultTools() {
        long count = externalToolMapper.selectCount(null);
        if (count == 0) {
            log.info("Initializing default external tools...");
            
            // Notion
            createTool("Notion", "一站式工作空间，用于笔记、文档和项目管理",
                    "https://www.notion.so", "notion", "#000000",
                    ExternalTool.ToolCategory.PRODUCTIVITY, 1);
            
            // Baidu
            createTool("百度", "中国最大的搜索引擎",
                    "https://www.baidu.com", "baidu", "#2932E1",
                    ExternalTool.ToolCategory.SEARCH, 2);
            
            // Google
            createTool("Google", "全球最大的搜索引擎",
                    "https://www.google.com", "google", "#4285F4",
                    ExternalTool.ToolCategory.SEARCH, 3);
            
            // GitHub
            createTool("GitHub", "全球最大的代码托管平台",
                    "https://github.com", "github", "#24292E",
                    ExternalTool.ToolCategory.DEVELOPMENT, 4);
            
            // Stack Overflow
            createTool("Stack Overflow", "程序员问答社区",
                    "https://stackoverflow.com", "stackoverflow", "#F48024",
                    ExternalTool.ToolCategory.DEVELOPMENT, 5);
            
            // Bilibili
            createTool("哔哩哔哩", "中国年轻人的视频社区",
                    "https://www.bilibili.com", "bilibili", "#FB7299",
                    ExternalTool.ToolCategory.ENTERTAINMENT, 6);
            
            // YouTube
            createTool("YouTube", "全球最大的视频分享平台",
                    "https://www.youtube.com", "youtube", "#FF0000",
                    ExternalTool.ToolCategory.ENTERTAINMENT, 7);
            
            // Twitter/X
            createTool("X (Twitter)", "全球社交媒体平台",
                    "https://x.com", "twitter", "#1DA1F2",
                    ExternalTool.ToolCategory.SOCIAL, 8);
            
            log.info("Default external tools initialized");
        }
    }
}

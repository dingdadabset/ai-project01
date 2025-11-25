package com.aiproject.module.page.service;

import com.aiproject.module.page.mapper.PageMapper;
import com.aiproject.module.page.model.Page;
import com.aiproject.module.user.mapper.UserMapper;
import com.aiproject.module.user.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Page Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PageService extends ServiceImpl<PageMapper, Page> {

    private final PageMapper pageMapper;
    private final UserMapper userMapper;

    @Transactional
    public Page createPage(String title, String content, String originalContent, Long authorId, Page.PageStatus status) {
        log.info("Creating page: {}", title);
        
        User author = userMapper.selectById(authorId);
        if (author == null) {
            throw new RuntimeException("Author not found");
        }
        
        Page page = Page.builder()
                .title(title)
                .slug(generateSlug(title))
                .content(content)
                .originalContent(originalContent)
                .authorId(authorId)
                .status(status != null ? status : Page.PageStatus.DRAFT)
                
                
                .build();
        
        pageMapper.insert(page);
        return page;
    }

    public Page getPageById(Long id) {
        Page page = pageMapper.selectById(id);
        if (page == null) {
            throw new RuntimeException("Page not found");
        }
        return page;
    }

    public Page getPageBySlug(String slug) {
        Page page = pageMapper.selectOne(new LambdaQueryWrapper<Page>().eq(Page::getSlug, slug));
        if (page == null) {
            throw new RuntimeException("Page not found");
        }
        return page;
    }

    public List<Page> getAllPages() {
        return pageMapper.selectList(null);
    }

    public IPage<Page> listPages(int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Page> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page + 1, size);
        return pageMapper.selectPage(pageObj, 
                new LambdaQueryWrapper<Page>().orderByDesc(Page::getCreatedAt));
    }

    public IPage<Page> listPublishedPages(int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Page> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page + 1, size);
        return pageMapper.selectPage(pageObj,
                new LambdaQueryWrapper<Page>()
                        .eq(Page::getStatus, Page.PageStatus.PUBLISHED)
                        .orderByDesc(Page::getCreatedAt));
    }

    @Transactional
    public Page updatePage(Long id, String title, String content, String originalContent, Page.PageStatus status) {
        Page page = getPageById(id);
        if (title != null) {
            page.setTitle(title);
            page.setSlug(generateSlug(title));
        }
        if (content != null) page.setContent(content);
        if (originalContent != null) page.setOriginalContent(originalContent);
        if (status != null) page.setStatus(status);
        
        pageMapper.updateById(page);
        return page;
    }

    @Transactional
    public void incrementViewCount(Long id) {
        Page page = getPageById(id);
        page.setViewCount(page.getViewCount() + 1);
        pageMapper.updateById(page);
    }

    @Transactional
    public void deletePage(Long id) {
        Page page = getPageById(id);
        pageMapper.deleteById(page.getId());
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();
        
        String baseSlug = slug;
        int counter = 1;
        while (pageMapper.selectCount(new LambdaQueryWrapper<Page>().eq(Page::getSlug, slug)) > 0) {
            slug = baseSlug + "-" + counter++;
        }
        
        return slug;
    }
}

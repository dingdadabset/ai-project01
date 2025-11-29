package com.aiproject.module.category.service;

import com.aiproject.module.category.mapper.CategoryMapper;
import com.aiproject.module.category.model.Category;
import com.aiproject.module.post.mapper.PostMapper;
import com.aiproject.module.post.model.Post;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Category Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    private final CategoryMapper categoryMapper;
    private final PostMapper postMapper;

    @Transactional
    public Category createCategory(String name, String description) {
        log.info("Creating category: {}", name);
        
        Category category = Category.builder()
                .name(name)
                .slug(generateSlug(name))
                .description(description)
                .build();
        
        categoryMapper.insert(category);
        return category;
    }

    public Category getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("Category not found");
        }
        // Calculate post count dynamically
        Long count = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getCategoryId, id));
        category.setPostCount(count != null ? count.intValue() : 0);
        return category;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = categoryMapper.selectList(null);
        // Calculate post count for each category
        for (Category category : categories) {
            Long count = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getCategoryId, category.getId()));
            category.setPostCount(count != null ? count.intValue() : 0);
        }
        return categories;
    }

    /**
     * List all categories (for theme context)
     */
    public List<Category> listCategories() {
        List<Category> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>().orderByAsc(Category::getName)
        );
        // Calculate post count for each category
        for (Category category : categories) {
            Long count = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getCategoryId, category.getId()));
            category.setPostCount(count != null ? count.intValue() : 0);
        }
        return categories;
    }

    public IPage<Category> listCategories(int page, int size) {
        Page<Category> categoryPage = new Page<>(page + 1, size);
        IPage<Category> result = categoryMapper.selectPage(categoryPage, 
                new LambdaQueryWrapper<Category>().orderByDesc(Category::getCreatedAt));
        // Calculate post count for each category
        for (Category category : result.getRecords()) {
            Long count = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getCategoryId, category.getId()));
            category.setPostCount(count != null ? count.intValue() : 0);
        }
        return result;
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description) {
        Category category = getCategoryById(id);
        category.setName(name);
        category.setSlug(generateSlug(name));
        category.setDescription(description);
        categoryMapper.updateById(category);
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryMapper.deleteById(category.getId());
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();
        
        String baseSlug = slug;
        int counter = 1;
        while (categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getSlug, slug)) > 0) {
            slug = baseSlug + "-" + counter++;
        }
        
        return slug;
    }
}

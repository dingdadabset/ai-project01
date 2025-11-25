package com.aiproject.module.category.service;

import com.aiproject.module.category.mapper.CategoryMapper;
import com.aiproject.module.category.model.Category;
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
        return category;
    }

    public List<Category> getAllCategories() {
        return categoryMapper.selectList(null);
    }

    public IPage<Category> listCategories(int page, int size) {
        Page<Category> categoryPage = new Page<>(page + 1, size);
        return categoryMapper.selectPage(categoryPage, 
                new LambdaQueryWrapper<Category>().orderByDesc(Category::getCreatedAt));
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

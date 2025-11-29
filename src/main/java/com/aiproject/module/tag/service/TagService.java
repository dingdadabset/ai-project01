package com.aiproject.module.tag.service;

import com.aiproject.module.post.mapper.PostTagMapper;
import com.aiproject.module.post.model.PostTag;
import com.aiproject.module.tag.mapper.TagMapper;
import com.aiproject.module.tag.model.Tag;
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
 * Tag Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagService extends ServiceImpl<TagMapper, Tag> {

    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;

    @Transactional
    public Tag createTag(String name) {
        log.info("Creating tag: {}", name);
        
        Tag tag = Tag.builder()
                .name(name)
                .slug(generateSlug(name))
                .build();
        
        tagMapper.insert(tag);
        return tag;
    }

    public Tag getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new RuntimeException("Tag not found");
        }
        // Calculate post count dynamically
        Long count = postTagMapper.selectCount(new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, id));
        tag.setPostCount(count != null ? count.intValue() : 0);
        return tag;
    }

    public Tag getTagBySlug(String slug) {
        Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getSlug, slug));
        if (tag == null) {
            throw new RuntimeException("Tag not found");
        }
        // Calculate post count dynamically
        Long count = postTagMapper.selectCount(new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tag.getId()));
        tag.setPostCount(count != null ? count.intValue() : 0);
        return tag;
    }

    public Tag getTagByName(String name) {
        Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (tag == null) {
            throw new RuntimeException("Tag not found");
        }
        return tag;
    }

    public List<Tag> getAllTags() {
        List<Tag> tags = tagMapper.selectList(null);
        // Calculate post count for each tag
        for (Tag tag : tags) {
            Long count = postTagMapper.selectCount(new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tag.getId()));
            tag.setPostCount(count != null ? count.intValue() : 0);
        }
        return tags;
    }

    /**
     * List all tags (for theme context)
     */
    public List<Tag> listTags() {
        List<Tag> tags = tagMapper.selectList(
            new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName)
        );
        // Calculate post count for each tag
        for (Tag tag : tags) {
            Long count = postTagMapper.selectCount(new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tag.getId()));
            tag.setPostCount(count != null ? count.intValue() : 0);
        }
        return tags;
    }

    public IPage<Tag> listTags(int page, int size) {
        Page<Tag> tagPage = new Page<>(page + 1, size);
        IPage<Tag> result = tagMapper.selectPage(tagPage, 
                new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getCreatedAt));
        // Calculate post count for each tag
        for (Tag tag : result.getRecords()) {
            Long count = postTagMapper.selectCount(new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tag.getId()));
            tag.setPostCount(count != null ? count.intValue() : 0);
        }
        return result;
    }

    @Transactional
    public Tag updateTag(Long id, String name) {
        Tag tag = getTagById(id);
        tag.setName(name);
        tag.setSlug(generateSlug(name));
        tagMapper.updateById(tag);
        return tag;
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = getTagById(id);
        tagMapper.deleteById(tag.getId());
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();
        
        String baseSlug = slug;
        int counter = 1;
        while (tagMapper.selectCount(new LambdaQueryWrapper<Tag>().eq(Tag::getSlug, slug)) > 0) {
            slug = baseSlug + "-" + counter++;
        }
        
        return slug;
    }
}

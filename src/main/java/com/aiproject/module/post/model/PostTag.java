package com.aiproject.module.post.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PostTag Entity
 * Represents the many-to-many relationship between posts and tags
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("post_tags")
public class PostTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("tag_id")
    private Long tagId;
}

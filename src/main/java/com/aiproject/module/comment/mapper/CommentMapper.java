package com.aiproject.module.comment.mapper;

import com.aiproject.module.comment.model.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Comment Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

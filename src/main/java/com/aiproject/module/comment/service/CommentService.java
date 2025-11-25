package com.aiproject.module.comment.service;

import com.aiproject.module.comment.mapper.CommentMapper;
import com.aiproject.module.comment.model.Comment;
import com.aiproject.module.post.mapper.PostMapper;
import com.aiproject.module.post.model.Post;
import com.aiproject.module.user.mapper.UserMapper;
import com.aiproject.module.user.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Comment Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Transactional
    public Comment createComment(Long postId, Long userId, String content, String guestName, String guestEmail) {
        log.info("Creating comment on post: {}", postId);
        
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }
        
        Comment.CommentBuilder builder = Comment.builder()
                .postId(postId)
                .content(content)
                .status(Comment.CommentStatus.PENDING)
                ;
        
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            builder.userId(userId);
        } else {
            builder.guestName(guestName).guestEmail(guestEmail);
        }
        
        Comment comment = builder.build();
        commentMapper.insert(comment);
        
        // Update post comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);
        
        return comment;
    }

    public IPage<Comment> getCommentsByPost(Long postId, int page, int size) {
        Page<Comment> commentPage = new Page<>(page + 1, size);
        return commentMapper.selectPage(commentPage,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .orderByDesc(Comment::getCreatedAt));
    }

    public Comment getCommentById(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("Comment not found");
        }
        return comment;
    }

    @Transactional
    public Comment approveComment(Long commentId) {
        Comment comment = getCommentById(commentId);
        comment.setStatus(Comment.CommentStatus.APPROVED);
        commentMapper.updateById(comment);
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = getCommentById(commentId);
        commentMapper.deleteById(commentId);
        
        // Update post comment count
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postMapper.updateById(post);
        }
    }
}

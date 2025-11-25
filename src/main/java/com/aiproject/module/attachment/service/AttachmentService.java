package com.aiproject.module.attachment.service;

import com.aiproject.module.attachment.mapper.AttachmentMapper;
import com.aiproject.module.attachment.model.Attachment;
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

import java.util.List;

/**
 * Attachment Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService extends ServiceImpl<AttachmentMapper, Attachment> {

    private final AttachmentMapper attachmentMapper;
    private final UserMapper userMapper;

    @Transactional
    public Attachment createAttachment(String name, String path, String url, String mediaType, 
                                       String suffix, Long size, Integer width, Integer height,
                                       Long uploaderId, Attachment.AttachmentType type) {
        log.info("Creating attachment: {}", name);
        
        User uploader = userMapper.selectById(uploaderId);
        if (uploader == null) {
            throw new RuntimeException("Uploader not found");
        }
        
        Attachment attachment = Attachment.builder()
                .name(name)
                .path(path)
                .url(url)
                .mediaType(mediaType)
                .suffix(suffix)
                .size(size)
                .width(width != null ? width : 0)
                .height(height != null ? height : 0)
                .uploaderId(uploaderId)
                .type(type != null ? type : Attachment.AttachmentType.OTHER)
                
                .build();
        
        attachmentMapper.insert(attachment);
        return attachment;
    }

    public Attachment getAttachmentById(Long id) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new RuntimeException("Attachment not found");
        }
        return attachment;
    }

    public List<Attachment> getAllAttachments() {
        return attachmentMapper.selectList(null);
    }

    public IPage<Attachment> listAttachments(int page, int size) {
        Page<Attachment> attachmentPage = new Page<>(page + 1, size);
        return attachmentMapper.selectPage(attachmentPage, 
                new LambdaQueryWrapper<Attachment>().orderByDesc(Attachment::getCreatedAt));
    }

    public IPage<Attachment> listAttachmentsByUploader(Long uploaderId, int page, int size) {
        Page<Attachment> attachmentPage = new Page<>(page + 1, size);
        return attachmentMapper.selectPage(attachmentPage,
                new LambdaQueryWrapper<Attachment>()
                        .eq(Attachment::getUploaderId, uploaderId)
                        .orderByDesc(Attachment::getCreatedAt));
    }

    public IPage<Attachment> listAttachmentsByType(Attachment.AttachmentType type, int page, int size) {
        Page<Attachment> attachmentPage = new Page<>(page + 1, size);
        return attachmentMapper.selectPage(attachmentPage,
                new LambdaQueryWrapper<Attachment>()
                        .eq(Attachment::getType, type)
                        .orderByDesc(Attachment::getCreatedAt));
    }

    @Transactional
    public Attachment updateAttachment(Long id, String name) {
        Attachment attachment = getAttachmentById(id);
        if (name != null) attachment.setName(name);
        attachmentMapper.updateById(attachment);
        return attachment;
    }

    @Transactional
    public void deleteAttachment(Long id) {
        Attachment attachment = getAttachmentById(id);
        attachmentMapper.deleteById(attachment.getId());
    }
}

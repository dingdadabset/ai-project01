package com.aiproject.module.attachment.repository;

import com.aiproject.module.attachment.model.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Attachment Repository
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    Page<Attachment> findByUploaderId(Long uploaderId, Pageable pageable);
    
    Page<Attachment> findByType(Attachment.AttachmentType type, Pageable pageable);
}

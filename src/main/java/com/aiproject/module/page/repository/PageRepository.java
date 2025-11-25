package com.aiproject.module.page.repository;

import com.aiproject.module.page.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Page Repository
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    
    Optional<Page> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
}

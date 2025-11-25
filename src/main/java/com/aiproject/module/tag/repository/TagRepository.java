package com.aiproject.module.tag.repository;

import com.aiproject.module.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Tag Repository
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findBySlug(String slug);
    
    Optional<Tag> findByName(String name);
    
    Set<Tag> findByNameIn(Set<String> names);
    
    boolean existsBySlug(String slug);
}

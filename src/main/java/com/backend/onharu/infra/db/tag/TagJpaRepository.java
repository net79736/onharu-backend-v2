package com.backend.onharu.infra.db.tag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.tag.model.Tag;

/**
 * 태그 JPA Repository
 */
public interface TagJpaRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByName(String name);
}

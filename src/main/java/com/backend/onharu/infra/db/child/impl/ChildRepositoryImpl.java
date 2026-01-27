package com.backend.onharu.infra.db.child.impl;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.repository.ChildRepository;
import com.backend.onharu.infra.db.child.ChildJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 아동 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ChildRepositoryImpl implements ChildRepository {

    private final ChildJpaRepository childJpaRepository;

    @Override
    public Child save(Child child) {
        return childJpaRepository.save(child);
    }
}

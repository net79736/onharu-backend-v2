package com.backend.onharu.infra.db.owner.impl;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사업자 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class OwnerRepositoryImpl implements OwnerRepository {

    private final OwnerJpaRepository ownerJpaRepository;

    @Override
    public Owner save(Owner owner) {
        return ownerJpaRepository.save(owner);
    }
}

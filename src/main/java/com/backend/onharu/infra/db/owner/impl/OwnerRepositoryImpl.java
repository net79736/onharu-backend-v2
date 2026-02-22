package com.backend.onharu.infra.db.owner.impl;

import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByIdParam;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByLoginIdParam;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

    @Override
    public Owner getOwnerById(GetOwnerByIdParam param) {
        return ownerJpaRepository.findById(param.id())
                .orElseThrow(() -> new CoreException(ErrorType.Owner.OWNER_NOT_FOUND));
    }

    @Override
    public Owner getOwnerByLoginId(GetOwnerByLoginIdParam param) {
        return ownerJpaRepository.findByUser_LoginId(param.loginId())
                .orElseThrow(() -> new CoreException(ErrorType.Owner.OWNER_NOT_FOUND));
    }

    @Override
    public void updateOwnerBusinessNumberById(OwnerRepositoryParam.UpdateOwnerBusinessNumberByIdParam param) {
        ownerJpaRepository.updateOwnerBusinessNumberById(
                param.businessNumber(),
                param.ownerId()
        );
    }
}
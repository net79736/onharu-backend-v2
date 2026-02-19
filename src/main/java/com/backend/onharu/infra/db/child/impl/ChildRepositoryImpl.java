package com.backend.onharu.infra.db.child.impl;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByIdParam;
import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByLoginIdParam;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.repository.ChildRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.child.ChildJpaRepository;

import lombok.RequiredArgsConstructor;

import static com.backend.onharu.domain.child.dto.ChildRepositoryParam.*;

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

    @Override
    public Child getChildById(GetChildByIdParam param) {
        return childJpaRepository.findById(param.id())
                .orElseThrow(() -> new CoreException(ErrorType.Child.CHILD_NOT_FOUND));
    }

    @Override
    public Child getChildByLoginId(GetChildByLoginIdParam param) {
        return childJpaRepository.findByUser_LoginId(param.loginId())
                .orElseThrow(() -> new CoreException(ErrorType.Child.CHILD_NOT_FOUND));
    }

    @Override
    public void updateChildNicknameById(UpdateChildNicknameByIdParam param) {
        childJpaRepository.updateChildNicknameById(
                param.childId(),
                param.nickname()
        );
    }
}

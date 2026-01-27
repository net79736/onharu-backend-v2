package com.backend.onharu.infra.db.user.impl;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByIdParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByLoginIdParam;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.repository.UserRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public User getUser(GetUserByIdParam param) {
        return userJpaRepository.findById(param.id())
                .orElseThrow(() -> new CoreException(ErrorType.User.USER_NOT_FOUND));
    }

    @Override
    public User getUserByLoginId(GetUserByLoginIdParam param) {
        return userJpaRepository.findByLoginId(param.loginId())
                .orElseThrow(() -> new CoreException(ErrorType.User.USER_NOT_FOUND));
    }

    @Override
    public boolean existsByLoginId(GetUserByLoginIdParam param) {
        return userJpaRepository.existsByLoginId(param.loginId());
    }
}

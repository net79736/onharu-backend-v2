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

import static com.backend.onharu.domain.user.dto.UserRepositoryParam.*;

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
        return userJpaRepository.findById(param.userId())
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

    @Override
    public User getUserByNameAndPhone(GetUserByNameAndPhoneParam param) {
        return userJpaRepository.findByNameAndPhone(param.name(), param.phone())
                .orElseThrow(() -> new CoreException(ErrorType.User.USER_NOT_FOUND));
    }

    @Override
    public void updateUserByIdAndPassword(UpdateUserByIdAndPasswordParam param) {
        userJpaRepository.resetPassword(param.id(), param.password());
    }

    @Override
    public void updateUserByIdAndNameAndPhone(UpdateUserByIdAndNameAndPhoneParam param) {
        userJpaRepository.updateUser(
                param.userId(),
                param.name(),
                param.phone()
        );
    }
}

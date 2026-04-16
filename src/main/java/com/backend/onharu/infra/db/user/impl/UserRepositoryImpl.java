package com.backend.onharu.infra.db.user.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByIdParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByLoginIdParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByNameAndPhoneParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.SearchUsersByLoginIdLikeParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.UpdateDeletedUserParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.UpdateUserByIdAndPasswordParam;
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
    public void updateDeletedUser(UpdateDeletedUserParam param) {
        userJpaRepository.updateDeletedUser(param.userId(), param.statusType());
    }

    /**
     * 로그인 ID에 키워드가 포함된 활성 사용자를 조회합니다 (본인 제외, 최대 20건).
     */
    @Override
    public List<User> searchUsersByLoginIdLike(SearchUsersByLoginIdLikeParam param) {
        return userJpaRepository.searchUsersByLoginIdLike(
                param.keyword(),
                param.excludeUserId(),
                StatusType.ACTIVE
        );
    }
}

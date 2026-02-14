package com.backend.onharu.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByIdQuery;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByLoginIdQuery;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByNameAndPhoneQuery;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByIdParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByLoginIdParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByNameAndPhoneParam;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 Query Service
 * 
 * 사용자 도메인의 상태를 조회하는 비즈니스 로직을 처리하는 서비스입니다.
 * Query 패턴을 사용하여 도메인 모델의 조회 작업을 캡슐화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 사용자를 조회합니다.
     * 
     * @param query 사용자 ID를 포함한 Query
     * @return 조회된 사용자 엔티티
     * @throws CoreException 사용자를 찾을 수 없는 경우
     */
    public User getUser(GetUserByIdQuery query) {
        return userRepository.getUser(new GetUserByIdParam(query.id()));
    }

    /**
     * 로그인 ID로 사용자를 조회합니다.
     * 
     * @param query 로그인 ID를 포함한 Query
     * @return 조회된 사용자 엔티티 (없으면 null)
     */
    public User getUserByLoginId(GetUserByLoginIdQuery query) {
        return userRepository.getUserByLoginId(new GetUserByLoginIdParam(query.loginId()));
    }

    /**
     * 이름과 전화번호로 사용자를 조회합니다.
     *
     * @param query 이름, 전화번호를 포함한 Query
     * @return 조회된 사용자 엔티티
     * @throws CoreException 사용자를 찾을 수 없는 경우
     */
    public User getUserByNameAndPhone(GetUserByNameAndPhoneQuery query) {
        return userRepository.getUserByNameAndPhone(new GetUserByNameAndPhoneParam(query.name(), query.phone()));
    }
}

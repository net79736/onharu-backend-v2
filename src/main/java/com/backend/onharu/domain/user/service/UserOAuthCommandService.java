package com.backend.onharu.domain.user.service;

import com.backend.onharu.domain.user.dto.UserOAuthCommand.CreateUserOAuth;
import com.backend.onharu.domain.user.model.UserOAuth;
import com.backend.onharu.domain.user.repository.UserOAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 사용자 Command Service
 * <p>
 * 소셜 사용자 도메인의 상태를 변경하는 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserOAuthCommandService {

    private final UserOAuthRepository userOAuthRepository;

    /**
     * 소셜 사용자 생성
     *
     * @param command 소셜 사용자 생성 command
     * @return 저장할 소셜 사용자 엔티티
     */
    public UserOAuth createUserOAuth(CreateUserOAuth command) {

        UserOAuth userOAuth = UserOAuth.builder()
                .providerId(command.providerId())
                .providerType(command.providerType())
                .build();
        userOAuth.addUser(command.user());

        return userOAuthRepository.save(userOAuth);
    }
}
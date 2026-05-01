package com.backend.onharu.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByNameAndPhoneQuery;
import com.backend.onharu.domain.user.dto.UserQuery.ValidatePasswordQuery;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthFacade 통합 테스트")
class AuthFacadeTest {

    @Autowired
    private AuthFacade authFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User saveActiveUser(String name, String phone, String rawPassword) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("auth-" + UUID.randomUUID().toString().substring(0, 8))
                        .password(passwordEncoder.encode(rawPassword))
                        .name(name)
                        .phone(phone)
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
    }

    @Nested
    @DisplayName("findId")
    class FindId {

        @Test
        @DisplayName("이름+전화번호로 존재하는 사용자의 ID 를 반환한다")
        void findId_existsActive_returnsUser() {
            User saved = saveActiveUser("홍길동", "01011112222", "password123");

            User found = authFacade.findId(new GetUserByNameAndPhoneQuery("홍길동", "01011112222"));

            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getLoginId()).isEqualTo(saved.getLoginId());
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 CoreException 발생")
        void findId_notExists_throws() {
            assertThatThrownBy(() ->
                    authFacade.findId(new GetUserByNameAndPhoneQuery("없는사람", "01099999999")))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("validatePassword")
    class ValidatePassword {

        @Test
        @DisplayName("올바른 비밀번호는 true 반환")
        void validatePassword_correct_returnsTrue() {
            User saved = saveActiveUser("김검증", "01033334444", "correctPass123!");

            boolean result = authFacade.validatePassword(
                    new ValidatePasswordQuery(saved.getId(), "correctPass123!"));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("틀린 비밀번호는 CoreException 발생")
        void validatePassword_wrong_throws() {
            User saved = saveActiveUser("김검증2", "01055556666", "correctPass123!");

            assertThatThrownBy(() ->
                    authFacade.validatePassword(
                            new ValidatePasswordQuery(saved.getId(), "wrongPass456!")))
                    .isInstanceOf(CoreException.class);
        }
    }
}

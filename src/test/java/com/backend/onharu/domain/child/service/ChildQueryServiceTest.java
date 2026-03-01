package com.backend.onharu.domain.child.service;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import static com.backend.onharu.domain.child.dto.ChildQuery.GetChildByUserIdQuery;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ChildQueryServiceTest 테스트")
class ChildQueryServiceTest {

    @Autowired
    private ChildQueryService childQueryService;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @BeforeEach
    void setUp() {
        childJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    /**
     * 테스트용 User 생성
     */
    private User createUser(String loginId, String name, String phone) {
        return userJpaRepository.save(
                User.builder()
                        .loginId(loginId)
                        .password("password123!")
                        .name(name)
                        .phone(phone)
                        .userType(UserType.CHILD)
                        .providerType(ProviderType.LOCAL)
                        .statusType(StatusType.ACTIVE)
                        .build()
        );
    }

    /**
     * 테스트용 Child 생성
     */
    private Child createChild(User user, String nickname) {
        return childJpaRepository.save(
                Child.builder()
                        .user(user)
                        .nickname(nickname)
                        .build()
        );
    }

    @Nested
    @DisplayName("getChildById 테스트")
    class getChildById {

        @Test
        @DisplayName("childId로 조회 성공")
        void shouldGetChildById() {
            // GIVEN
            User user = createUser("test1@test.com", "이름테스트1", "01011112222");
            Child child = createChild(user, "닉네임테스트1");

            GetChildByIdQuery query = new GetChildByIdQuery(child.getId());

            // WHEN
            Child result = childQueryService.getChildById(query);

            // THEN
            assertThat(result.getId()).isEqualTo(child.getId());
            assertThat(result.getNickname()).isEqualTo("닉네임테스트1");
            assertThat(result.getUser().getId()).isEqualTo(user.getId());
        }
    }

    @Nested
    @DisplayName("getChildByUserId 테스트")
    class getChildByUserId {

        @Test
        @DisplayName("userId로 조회 성공")
        void shouldGetChildByUserId() {
            // GIVEN
            User user = createUser("test3@test.com", "이름3", "01033334444");
            Child child = createChild(user, "닉네임3");

            GetChildByUserIdQuery query = new GetChildByUserIdQuery(user.getId());

            // WHEN
            Child result = childQueryService.getChildByUserId(query);

            // THEN
            assertThat(result.getId()).isEqualTo(child.getId());
            assertThat(result.getUser().getId()).isEqualTo(user.getId());
            assertThat(result.getNickname()).isEqualTo("닉네임3");
        }
    }
}
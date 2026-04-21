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
import org.springframework.transaction.annotation.Transactional;
import com.backend.onharu.domain.common.TestDataHelper;

import static com.backend.onharu.domain.child.dto.ChildCommand.CreateChildCommand;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ChildCommandService 테스트")
class ChildCommandServiceTest {

    @Autowired
    private ChildCommandService childCommandService;

    @Autowired


    private TestDataHelper testDataHelper;


    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @BeforeEach
    public void setUp() {

        testDataHelper.cleanAll();

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

    @Nested
    @DisplayName("createChild 테스트")
    class CreateChildTest {

        @Test
        @DisplayName("아동 생성 성공")
        void shouldCreateChild() {
            // GIVEN
            User user = createUser("test1234444444@test.com", "테스트이름", "01011112222");

            CreateChildCommand command = new CreateChildCommand(user, "테스트닉네임1234444444");

            // WHEN
            Child child = childCommandService.createChild(command);

            // THEN
            assertThat(child.getId()).isNotNull();
            assertThat(child.getUser().getId()).isEqualTo(user.getId());
            assertThat(child.getNickname()).isEqualTo("테스트닉네임1234444444");
            assertThat(child.getIsVerified()).isFalse();

            assertThat(childJpaRepository.findById(child.getId())).isPresent();
        }
    }
}
package com.backend.onharu.domain.owner.service;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
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

import static com.backend.onharu.domain.owner.dto.OwnerCommand.CreateOwnerCommand;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OwnerCommandService 테스트")
class OwnerCommandServiceTest {

    @Autowired
    private OwnerCommandService ownerCommandService;

    @Autowired


    private TestDataHelper testDataHelper;


    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @BeforeEach
    void setUp() {

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
                        .userType(UserType.OWNER)
                        .providerType(ProviderType.LOCAL)
                        .statusType(StatusType.ACTIVE)
                        .build()
        );
    }

    /**
     * 테스트용 Level 생성
     */
    private Level createLevel(String levelName) {
        return levelJpaRepository.save(
                Level.builder()
                        .name(levelName)
                        .build()
        );
    }

    @Nested
    @DisplayName("createOwner 테스트")
    class CreateOwnerTest {

        @Test
        @DisplayName("사업자 생성 성공")
        void shouldCreateOwner() {
            // GIVEN
            User user = createUser("owner999999@test.com", "사업자이름99", "01011112222");
            Level level = createLevel("비기너");

            CreateOwnerCommand command = new CreateOwnerCommand(user, level, "1234567890");

            // WHEN
            Owner owner = ownerCommandService.createOwner(command);

            // THEN
            assertThat(owner.getId()).isNotNull();
            assertThat(owner.getUser().getId()).isEqualTo(user.getId());
            assertThat(owner.getLevel().getId()).isEqualTo(level.getId());
            assertThat(owner.getBusinessNumber()).isEqualTo("1234567890");

            assertThat(ownerJpaRepository.findById(owner.getId())).isPresent();
        }
    }
}
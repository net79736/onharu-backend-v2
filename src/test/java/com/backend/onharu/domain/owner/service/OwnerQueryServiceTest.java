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

import static com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import static com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByUserIdQuery;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OwnerQueryServiceTest 테스트")
class OwnerQueryServiceTest {

    @Autowired
    private OwnerQueryService ownerQueryService;

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
//        childJpaRepository.deleteAll();
//        ownerJpaRepository.deleteAll();
//        userJpaRepository.deleteAll();
//        levelJpaRepository.deleteAll();
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

    /**
     * 테스트용 Owner 생성
     */
    private Owner createOwner(User user, Level level, String businessNumber) {
        return ownerJpaRepository.save(
                Owner.builder()
                        .user(user)
                        .level(level)
                        .businessNumber(businessNumber)
                        .build()
        );
    }

    @Nested
    @DisplayName("getOwnerById 테스트")
    class getOwnerById {

        @Test
        @DisplayName("ownerId로 조회 성공")
        void shouldGetOwnerById() {
            // GIVEN
            User user = createUser("owner1111111@test.com", "사업자1111111", "01011112222");
            Level level = createLevel("비기너");
            Owner owner = createOwner(user, level, "1234567890");

            GetOwnerByIdQuery query = new GetOwnerByIdQuery(owner.getId());

            // WHEN
            Owner result = ownerQueryService.getOwnerById(query);

            // THEN
            assertThat(result.getId()).isEqualTo(owner.getId());
            assertThat(result.getBusinessNumber()).isEqualTo("1234567890");
            assertThat(result.getUser().getId()).isEqualTo(user.getId());
            assertThat(result.getLevel().getId()).isEqualTo(level.getId());
        }
    }

    @Nested
    @DisplayName("getOwnerByUserId 테스트")
    class getOwnerByUserId {

        @Test
        @DisplayName("userId로 조회 성공")
        void shouldGetOwnerByUserId() {
            // GIVEN
            User user = createUser("owner2221@test.com", "사업자2221", "01033334444");
            Level level = createLevel("비기너");
            Owner owner = createOwner(user, level, "9876543210");

            GetOwnerByUserIdQuery query = new GetOwnerByUserIdQuery(user.getId());

            // WHEN
            Owner result = ownerQueryService.getOwnerByUserId(query);

            // THEN
            assertThat(result.getId()).isEqualTo(owner.getId());
            assertThat(result.getUser().getId()).isEqualTo(user.getId());
            assertThat(result.getBusinessNumber()).isEqualTo("9876543210");
            assertThat(result.getLevel().getId()).isEqualTo(level.getId());
        }
    }
}
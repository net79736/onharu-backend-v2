package com.backend.onharu.domain.favorite.service;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.favorite.dto.FavoriteCommand.CreateFavoriteCommand;
import static com.backend.onharu.domain.favorite.dto.FavoriteCommand.DeleteFavoriteCommand;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * FavoriteCommandService 의 테스트 코드 입니다.
 */
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class FavoriteCommandServiceTest {

    @Autowired
    private FavoriteCommandService favoriteCommandService;

    @Autowired
    private FavoriteJpaRepository favoriteJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    /**
     * 테스트용 Level 생성
     */
    private Level createTestLevel(String name) {
        return levelJpaRepository.save(
                Level.builder()
                        .name(name)
                        .build()
        );
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드 (사업자용)
     */
    private User createTestUserForOwner(String loginId, String name, String phone) {
        return userJpaRepository.save(
                User.builder()
                        .loginId(loginId)
                        .password("password123")
                        .name(name)
                        .phone(phone)
                        .userType(UserType.OWNER)
                        .providerType(ProviderType.LOCAL)
                        .statusType(StatusType.ACTIVE)
                        .build()
        );
    }

    /**
     * 테스트용 Owner 생성
     */
    private Owner createTestOwner(String loginId, String name, String phone, String businessNumber, Level level) {
        User user = createTestUserForOwner(loginId, name, phone);

        return ownerJpaRepository.save(
                Owner.builder()
                        .user(user)
                        .level(level)
                        .businessNumber(businessNumber)
                        .build()
        );
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드 (아동용)
     */
    private User createTestUserForChild(String loginId, String name, String phone) {
        return userJpaRepository.save(
                User.builder()
                        .loginId(loginId)
                        .password("password123")
                        .name(name)
                        .phone(phone)
                        .providerType(ProviderType.LOCAL)
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .build()
        );
    }

    /**
     * 테스트용 Child 생성
     */
    private Child createTestChild(String loginId, String name, String phone, String nickname, Boolean isVerified) {
        User user = createTestUserForChild(loginId, name, phone);

        return childJpaRepository.save(
                Child.builder()
                        .user(user)
                        .nickname(nickname)
                        .isVerified(isVerified)
                        .build()
        );
    }

    /**
     * 테스트용 Category 생성
     */
    private Category createTestCategory(String name) {
        return categoryJpaRepository.save(
                Category.builder()
                        .name(name)
                        .build()
        );
    }

    /**
     * 테스트용 Store 생성
     */
    private Store createTestStore(String name, Owner owner, Category category) {
        return storeJpaRepository.save(Store.builder()
                .name(name)
                .owner(owner)
                .category(category)
                .address("서울시 강남구")
                .phone("0200000000")
                .isOpen(true)
                .build()
        );
    }

    @Nested
    @DisplayName("찜하기 생성 테스트")
    class CreateFavoriteTest {

        @Test
        @DisplayName("찜하기 생성 성공")
        void createFavorite() {
            // GIVEN
            Child child = createTestChild(
                    "favoriteChild1@test.com",
                    "찜하기아동테스트1",
                    "01099990010",
                    "찜하기아동닉네임1",
                    true
            );
            Level level = createTestLevel("찜하기등급1");
            Owner owner = createTestOwner(
                    "favoriteOwner1@test.com",
                    "찜하기사업자테스트1",
                    "01099990011",
                    "9999999991",
                    level
            );
            Category category = createTestCategory("식당");
            Store store = createTestStore(
                    "찜하기가게테스트1",
                    owner,
                    category
            );

            CreateFavoriteCommand command = new CreateFavoriteCommand(child, store);

            // WHEN
            Favorite favorite = favoriteCommandService.createFavorite(command);

            // THEN
            assertThat(favorite.getChild().getId()).isEqualTo(child.getId());
            assertThat(favorite.getStore().getId()).isEqualTo(store.getId());
        }
    }

    @Nested
    @DisplayName("찜하기 삭제 테스트")
    class deleteFavoriteTest {

        @Test
        @DisplayName("찜하기 삭제 성공")
        void deleteFavorite() {
            //GIVEN
            Child child = createTestChild(
                    "favoriteChild2@test.com",
                    "찜하기아동테스트2",
                    "01099990020",
                    "찜하기아동닉네임2",
                    true
            );
            Level level = createTestLevel("찜하기등급2");
            Owner owner = createTestOwner(
                    "favoriteOwner2@test.com",
                    "찜하기사업자테스트2",
                    "01099990021",
                    "9999999992",
                    level
            );
            Category category = createTestCategory("카페");
            Store store = createTestStore(
                    "찜하기가게테스트2",
                    owner,
                    category
            );

            Favorite favorite = favoriteJpaRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store)
                            .build()
            );

            DeleteFavoriteCommand command = new DeleteFavoriteCommand(favorite);

            // WHEN
            favoriteCommandService.deleteFavorite(command);

            // THEN
            assertThat(favoriteJpaRepository.findById(favorite.getId())).isEmpty();
        }
    }
}
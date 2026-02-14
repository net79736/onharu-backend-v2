package com.backend.onharu.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.favorite.repository.FavoriteRepository;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class FavoriteQueryServiceTest {

    @Autowired
    private FavoriteQueryService favoriteQueryService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FavoriteJpaRepository favoriteJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @BeforeEach
    void setUp() {
        favoriteJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        levelJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
    }

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
    private Child createTestChild(String loginId, String name, String phone, String nickname, String certificate, Boolean isVerified) {
        User user = createTestUserForChild(loginId, name, phone);

        return childJpaRepository.save(
                Child.builder()
                        .user(user)
                        .nickname(nickname)
                        .certificate(certificate)
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
    @DisplayName("찜하기 ID 단건 조회 테스트")
    class GetFavoriteTest {

        @Test
        @DisplayName("찜하기 단건 조회 성공")
        void getFavorite() {
            // GIVEN
            Child child = createTestChild(
                    "child@test.com",
                    "아동테스트",
                    "01022223333",
                    "닉네임테스트",
                    "/certificate/1.pdf",
                    true
            );
            Level level = createTestLevel("줄기");
            Owner owner = createTestOwner(
                    "owner@test.com",
                    "사업자테스트",
                    "01099998888",
                    "1234567890",
                    level
            );
            Category category = createTestCategory("식당");
            Store store = createTestStore(
                    "가게테스트",
                    owner,
                    category
            );

            Favorite savedFavorite = favoriteJpaRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store)
                            .build()
            );

            FavoriteQuery.GetFavoriteByIdQuery query = new FavoriteQuery.GetFavoriteByIdQuery(savedFavorite.getId());

            // WHEN
            Favorite favorite = favoriteQueryService.getFavorite(query);

            // THEN
            assertThat(favorite.getId()).isEqualTo(savedFavorite.getId());
            assertThat(favorite.getChild().getId()).isEqualTo(child.getId());
            assertThat(favorite.getStore().getId()).isEqualTo(store.getId());
        }

        @Test
        @DisplayName("찜하기 ID 단건 조회 실패 - 존재하지 않는 찜하기 ID 로 조회시 예외 발생")
        void getFavorite_fail_whenFavoriteNotFound() {
            // GIVEN
            Long favoriteId = 123456789L;
            FavoriteQuery.GetFavoriteByIdQuery query = new FavoriteQuery.GetFavoriteByIdQuery(favoriteId);

            // WHEN
            assertThatThrownBy(() -> favoriteQueryService.getFavorite(query)).isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("아동 ID 로 찜하기 목록 조회")
    class FindFavoritesByChildId {

        @Test
        @DisplayName("찜하기 목록 조회 성공")
        void findFavoritesByChildId() {
            // GIVEN
            Child child = createTestChild(
                    "child@test.com",
                    "아동테스트",
                    "01022223333",
                    "닉네임테스트",
                    "/certificate/1.pdf",
                    true
            );
            Level level = createTestLevel("줄기");
            Owner owner1 = createTestOwner(
                    "owner1@test.com",
                    "사업자테스트1",
                    "01000000001",
                    "0000000001",
                    level
            );
            Owner owner2 = createTestOwner(
                    "owner2@test.com",
                    "사업자테스트2",
                    "01000000002",
                    "0000000002",
                    level
            );
            Category category = createTestCategory("식당");
            Store store1 = createTestStore(
                    "가게테스트1",
                    owner1,
                    category
            );
            Store store2 = createTestStore(
                    "가게테스트2",
                    owner2,
                    category
            );

            favoriteRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store1)
                            .build()
            );

            favoriteRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store2)
                            .build()
            );
            FavoriteQuery.FindFavoritesByChildIdQuery query = new FavoriteQuery.FindFavoritesByChildIdQuery(child.getId());

            // WHEN
            List<Favorite> favorites = favoriteQueryService.findFavoritesByChildId(query);

            // THEN
            assertThat(favorites).hasSize(2);
            assertThat(favorites).allMatch(favorite -> favorite
                    .getChild()
                    .getId()
                    .equals(child.getId()));
        }

        @Test
        @DisplayName("찜하기 목록 조회 실패 - 찜하기가 없는 경우 빈 리스트 반환")
        void findFavoritesByChildId_fail_emptyResult() {
            // GIVEN
            Child child = createTestChild(
                    "child@test.com",
                    "아동테스트",
                    "01022223333",
                    "닉네임테스트",
                    "/certificate/1.pdf",
                    true
            );

            FavoriteQuery.FindFavoritesByChildIdQuery query = new FavoriteQuery.FindFavoritesByChildIdQuery(child.getId());

            // WHEN
            List<Favorite> favorites = favoriteQueryService.findFavoritesByChildId(query);

            // THEN
            assertThat(favorites).isEmpty();
        }
    }
}
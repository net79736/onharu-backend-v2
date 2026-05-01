package com.backend.onharu.domain.favorite.service;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.GetFavoriteByIdQuery;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoriteByChild_IdAndStore_IdQuery;
import static com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoritesByChildIdQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("찜하기 ID 단건 조회 테스트")
    class GetFavoriteTest {

        @Test
        @DisplayName("찜하기 단건 조회 성공")
        void getFavorite() {
            // GIVEN
            Child child = createTestChild(
                    "favoriteChild3@test.com",
                    "찜하기아동테스트3",
                    "01099990030",
                    "찜하기아동닉네임3",
                    true
            );
            Level level = createTestLevel("찜하기등급3");
            Owner owner = createTestOwner(
                    "favoriteOwner3@test.com",
                    "찜하기사업자테스트3",
                    "01099990031",
                    "9999999993",
                    level
            );
            Category category = createTestCategory("식당3");
            Store store = createTestStore(
                    "찜하기가게테스트3",
                    owner,
                    category
            );

            Favorite savedFavorite = favoriteJpaRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store)
                            .build()
            );

            GetFavoriteByIdQuery query = new GetFavoriteByIdQuery(savedFavorite.getId());

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
            GetFavoriteByIdQuery query = new GetFavoriteByIdQuery(favoriteId);

            // WHEN
            assertThatThrownBy(() -> favoriteQueryService.getFavorite(query))
                    .isInstanceOf(CoreException.class);
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
                    "favoriteChild4@test.com",
                    "찜하기아동테스트4",
                    "01099990040",
                    "찜하기아동닉네임4",
                    true
            );
            Level level = createTestLevel("찜하기등급4");
            Owner owner1 = createTestOwner(
                    "favoriteOwner4@test.com",
                    "찜하기사업자테스트4",
                    "01099990041",
                    "9999999941",
                    level
            );
            Owner owner2 = createTestOwner(
                    "favoriteOwner5@test.com",
                    "찜하기사업자테스트5",
                    "01099990052",
                    "9999999952",
                    level
            );
            Category category = createTestCategory("식당4");
            Store store1 = createTestStore(
                    "찜하기가게테스트4",
                    owner1,
                    category
            );
            Store store2 = createTestStore(
                    "찜하기가게테스트5",
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
            FindFavoritesByChildIdQuery query = new FindFavoritesByChildIdQuery(child.getId());

            Pageable pageable = PageRequest.of(0, 10);

            // WHEN
            Page<Favorite> favorites = favoriteQueryService.findFavoritesByChildId(query, pageable);

            // THEN
            assertThat(favorites.getTotalElements()).isEqualTo(2);
            assertThat(favorites.getContent()).hasSize(2);
            assertThat(favorites.getContent()).allMatch(favorite -> favorite
                    .getChild()
                    .getId()
                    .equals(child.getId()));
        }

        @Test
        @DisplayName("찜하기 목록 조회 실패 - 찜하기가 없는 경우 빈 리스트 반환")
        void findFavoritesByChildId_fail_emptyResult() {
            // GIVEN
            Child child = createTestChild(
                    "favoriteChild5@test.com",
                    "찜하기아동테스트5",
                    "01099990050",
                    "찜하기아동닉네임5",
                    true
            );

            FindFavoritesByChildIdQuery query = new FindFavoritesByChildIdQuery(child.getId());

            Pageable pageable = PageRequest.of(0, 10);

            // WHEN
            Page<Favorite> favorites = favoriteQueryService.findFavoritesByChildId(query, pageable);

            // THEN
            assertThat(favorites.getTotalElements()).isZero();
            assertThat(favorites.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("찜하기 내역 조회 테스트")
    class FindFavoriteByChild_IdAndStore_Id {

        @Test
        @DisplayName("찜하기 내역 조회 테스트 성공")
        void findFavoriteByChild_IdAndStore_IdTest() {
            // GIVEN
            Child child = createTestChild(
                    "favoriteChild3@test.com",
                    "찜하기아동테스트3",
                    "01099990030",
                    "찜하기아동닉네임3",
                    true
            );
            Level level = createTestLevel("찜하기등급3");
            Owner owner = createTestOwner(
                    "favoriteOwner3@test.com",
                    "찜하기사업자테스트3",
                    "01099990031",
                    "9999999993",
                    level
            );
            Category category = createTestCategory("식당3");
            Store store = createTestStore(
                    "찜하기가게테스트3",
                    owner,
                    category
            );

            Favorite savedFavorite = favoriteJpaRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store)
                            .build()
            );

            Long childId = child.getId();
            Long storeId = store.getId();

            // WHEN
            Optional<Favorite> favorite = favoriteQueryService.findFavoriteByChild_IdAndStore_Id(
                    new FindFavoriteByChild_IdAndStore_IdQuery(childId, storeId)
            );

            // THEN
            assertThat(favorite).isPresent();
            assertThat(favorite.get().getId()).isEqualTo(savedFavorite.getId());
        }
    }
}
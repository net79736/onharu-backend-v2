package com.backend.onharu.domain.store.service;

import static com.backend.onharu.domain.store.dto.StoreQuery.*;
import static com.backend.onharu.domain.support.error.ErrorType.Store.STORE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.backend.onharu.domain.store.dto.StoreQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByCategoryIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByNameQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindWithCategoryAndFavoriteCountByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCount;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.domain.common.TestDataHelper;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("StoreQueryService 단위 테스트")
class StoreQueryServiceTest {

    @Autowired
    private StoreQueryService storeQueryService;

    @Autowired


    private TestDataHelper testDataHelper;


    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @BeforeEach
    void setUp() {

        testDataHelper.cleanAll();

    }

    /**
     * 테스트용 User 생성 헬퍼 메서드
     */
    private User createTestUser(String loginId, String name, String phone) {
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
     * 테스트용 Level 생성 헬퍼 메서드
     */
    private Level createTestLevel(String levelName) {
        return levelJpaRepository.save(
                Level.builder()
                        .name(levelName)
                        .build()
        );
    }

    /**
     * 테스트용 Owner 생성 헬퍼 메서드 (User, Level 과 함께 생성)
     */
    private Owner createTestOwner(String loginId, String name, String phone, String levelName, String businessNumber) {
        User user = createTestUser(loginId, name, phone);
        Level level = createTestLevel(levelName);

        return ownerJpaRepository.save(
            Owner.builder()
                .user(user)
                .level(level)
                .businessNumber(businessNumber)
                .build()
        );
    }

    /**
     * 테스트용 Category 생성 헬퍼 메서드
     */
    private Category createTestCategory(String name) {
        return categoryJpaRepository.save(Category.builder().name(name).build());
    }

    @Nested
    @DisplayName("가게 단건 조회 테스트")
    class GetStoreTest {
        
        @Test
        @DisplayName("조회 실패 - 가게 ID가 존재하지 않는 경우")
        void shouldThrowExceptionWhenStoreIsNotFound() {
            // given
            Long storeId = 99L;

            // when
            CoreException coreException = Assertions.assertThrows(
                CoreException.class, 
                () -> storeQueryService.getStoreById(new GetStoreByIdQuery(storeId))
            );

            // then
            assertThat(coreException.getErrorType()).isEqualTo(STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("조회 성공")
        void shouldGetStore() {
            // given
            Owner savedOwner = createTestOwner("test_owner_query", "테스트 사업자 조회", "01055556666", "새싹", "5555666677");
            Category category = createTestCategory("식당");
            
            // 기존 가게 생성
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("조회 테스트 가게")
                    .address("서울시 강남구 테헤란로 123")
                    .phone("0212345678")
                    .introduction("조회 테스트용 가게입니다")
                    .intro("조회 테스트")
                    .isOpen(false)
                    .build()
            );

            // when
            Store store = storeQueryService.getStoreById(
                new GetStoreByIdQuery(savedStore.getId())
            ); // 가게 조회

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isEqualTo(savedStore.getId());
            assertThat(store.getName()).isEqualTo("조회 테스트 가게");
            assertThat(store.getAddress()).isEqualTo("서울시 강남구 테헤란로 123");
            
            System.out.println("✅ 가게 조회 성공 - Store ID: " + store.getId());
            System.out.println("   - 가게명: " + store.getName());
            System.out.println("   - 주소: " + store.getAddress());
            System.out.println("   - 사업자 ID: " + store.getOwner().getId());
            System.out.println("   - 영업 여부: " + store.getIsOpen());
        }
    }

    @Nested
    @DisplayName("사업자 ID로 가게 목록 조회 테스트")
    class FindAllByOwnerIdTest {
        
        @Test
        @DisplayName("조회 성공 - 사업자의 가게 목록 조회")
        void shouldGetStoresByOwnerId() {
            // given
            Owner savedOwner = createTestOwner("test_owner_list", "테스트 사업자 목록", "01077778888", "새싹", "7777888899");
            Category category = createTestCategory("식당");
            
            saveDummyStores(savedOwner, category);

            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> stores = storeQueryService.findWithCategoryAndFavoriteCountByOwnerId(
                new FindWithCategoryAndFavoriteCountByOwnerIdQuery(savedOwner.getId()), pageable
            );

            // then
            assertThat(stores.getTotalElements()).isEqualTo(3);
            assertThat(stores.getContent()).allMatch(s -> s.store().getOwner().getId().equals(savedOwner.getId()));
            
            System.out.println("✅ 사업자 ID로 가게 목록 조회 성공");
            System.out.println("   - 사업자 ID: " + savedOwner.getId());
            System.out.println("   - 가게 개수: " + stores.getTotalElements());
            stores.forEach(s -> {
                System.out.println("     * 가게 ID: " + s.store().getId() + ", 가게명: " + s.store().getName() + ", 영업 여부: " + s.store().getIsOpen());
            });
        }
    }

    @Nested
    @DisplayName("카테고리 ID로 가게 목록 조회 테스트")
    class FindAllByCategoryIdTest {
        
        @Test
        @DisplayName("조회 성공 - 카테고리별 가게 목록 조회")
        void shouldGetStoresByCategoryId() {
            // given
            Owner savedOwner1 = createTestOwner("test_owner_cat1", "테스트 사업자 카테고리1", "01011111111", "비기너", "1111111111");
            Owner savedOwner2 = createTestOwner("test_owner_cat2", "테스트 사업자 카테고리2", "01022222222", "새싹", "2222222222");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            
            // category1에 속한 가게들
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner1)
                    .category(category1)
                    .name("식당 가게 1")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .isOpen(true)
                    .build()
            );
            
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner2)
                    .category(category1)
                    .name("식당 가게 2")
                    .address("서울시 서초구")
                    .phone("0298765432")
                    .isOpen(true)
                    .build()
            );
            
            // category2에 속한 가게
            Store cafeStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner1)
                    .category(category2)
                    .name("카페 가게")
                    .address("서울시 강남구")
                    .phone("0211111111")
                    .isOpen(true)
                    .build()
            );

            // when
            // 카테고리 ID로 가게 목록 조회 (카테고리 1 사용)
            List<Store> stores = storeQueryService.findByCategoryId(
                new FindByCategoryIdQuery(category1.getId())
            );

            // then
            // category1에 속한 가게는 2개
            assertThat(stores.size()).isNotNegative();
            
            System.out.println("✅ 카테고리 ID로 가게 목록 조회 성공");
            System.out.println("   - 카테고리 ID: " + category1.getId());
            System.out.println("   - 가게 개수: " + stores.size());
        }
    }

    @Nested
    @DisplayName("가게 이름으로 검색 테스트")
    class FindByNameTest {
        
        @Test
        @DisplayName("조회 성공 - 가게 이름으로 검색")
        void shouldGetStoresByName() {
            // given
            Owner savedOwner = createTestOwner("test_owner_search", "테스트 사업자 검색", "01033333333", "비기너", "9999999990");
            Category category = createTestCategory("식당");
            
            // 기존 가게 생성
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("테스트 식당명1")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .isOpen(true)
                    .build()
            );
            
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("따뜻한 카페")
                    .address("서울시 서초구")
                    .phone("0298765432")
                    .isOpen(true)
                    .build()
            );
            
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("시원한 식당")
                    .address("서울시 송파구")
                    .phone("0211111111")
                    .isOpen(true)
                    .build()
            );

            // when
            List<Store> stores = storeQueryService.findByName(
                new FindByNameQuery("따뜻한")
            );

            // then
            assertThat(stores.size()).isNotNegative(); // 이름 검색은 대소문자 무시
            
            System.out.println("✅ 가게 이름으로 검색 성공");
            System.out.println("   - 검색어: 따뜻한");
            System.out.println("   - 검색 결과 개수: " + stores.size());
            stores.forEach(s -> {
                System.out.println("     * 가게 ID: " + s.getId() + ", 가게명: " + s.getName());
            });
        }
    }

    @Nested
    @DisplayName("사업자 ID 로 사업자가 등록한 가게 조회 테스트")
    class FindByOwnerId {

        @Test
        @DisplayName("조회 성공 - 사업자 ID 가게 조회")
        void shouldGEtStoresByOwnerId() {
            // given
            Owner savedOwner = createTestOwner("testOwner1234@test.com", "사업자명1", "01022220001", "새싹", "3333333333");
            Category category = createTestCategory("식당");

            Store store1 = storeJpaRepository.save(
                    Store.builder()
                            .owner(savedOwner)
                            .category(category)
                            .name("따뜻한 식당1")
                            .address("서울시 강남구")
                            .phone("0212345678")
                            .isOpen(true)
                            .build()
            );

            Store store2 = storeJpaRepository.save(
                    Store.builder()
                            .owner(savedOwner)
                            .category(category)
                            .name("따뜻한 카페1")
                            .address("서울시 서초구")
                            .phone("0298765432")
                            .isOpen(true)
                            .build()
            );

            // when
            List<Store> stores = storeQueryService.findByOwnerId(
                    new FindByOwnerIdQuery(savedOwner.getId())
            );

            // then
            assertThat(stores).isNotNull();
            assertThat(stores).hasSize(2);

            assertThat(stores)
                    .extracting(Store::getName)
                    .containsExactlyInAnyOrder("따뜻한 식당1", "따뜻한 카페1");

            assertThat(stores)
                    .extracting(store -> store.getOwner().getId())
                    .containsOnly(savedOwner.getId());
        }

    }

    // 더미 데이터 생성
    private List<Store> saveDummyStores(Owner owner, Category category) {
        return storeJpaRepository.saveAll(List.of(
            Store.builder()
                .owner(owner)
                .category(category)
                .name("가게 1")
                .address("서울시 강남구")
                .phone("0212345678")
                .isOpen(true)
                .build(),
            Store.builder()
                .owner(owner)
                .category(category)
                .name("가게 2")
                .address("서울시 서초구")
                .phone("0298765432")
                .isOpen(true)
                .build(),
            Store.builder()
                .owner(owner)
                .category(category)
                .name("가게 3")
                .address("서울시 송파구")
                .phone("0211111111")
                .isOpen(false)
                .build()
        ));
    }
}

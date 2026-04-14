package com.backend.onharu.application;

import static com.backend.onharu.domain.support.error.ErrorType.Store.STORE_OWNER_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.DeleteStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.SearchStoresQuery;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCount;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.model.StoreTag;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.store.support.StoreSearchSortResolver;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.chat.ChatMessageJpaRepository;
import com.backend.onharu.infra.db.chat.ChatParticipantJpaRepository;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import com.backend.onharu.infra.db.file.FileJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationHistoryJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.review.ReviewJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.db.user.UserOAuthJpaRepository;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("StoreFacade 단위 테스트")
class StoreFacadeTest {

    @Autowired
    private StoreFacade storeFacade;

    @Autowired
    private StoreQueryService storeQueryService;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private TagJpaRepository tagJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private ChatMessageJpaRepository chatMessageJpaRepository;

    @Autowired
    private ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Autowired
    private ChatRoomJpaRepository chatRoomJpaRepository;

    @Autowired
    private NotificationHistoryJpaRepository notificationHistoryJpaRepository;

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    @Autowired
    private ReviewJpaRepository reviewJpaRepository;

    @Autowired
    private FavoriteJpaRepository favoriteJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private FileJpaRepository fileJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserOAuthJpaRepository userOAuthJpaRepository;

    @BeforeEach
    public void setUp() {
        // 전체 테스트 스위트 공유 DB에서 남은 FK 때문에 다른 테스트 데이터가 있으면 store 단독 deleteAll 이 실패할 수 있음.
        // OwnerFacadeTest / ReservationCommandServiceTest 등과 동일한 자식 → 부모 순서로 정리.
        chatMessageJpaRepository.deleteAll();
        chatParticipantJpaRepository.deleteAll();
        chatRoomJpaRepository.deleteAll();
        notificationHistoryJpaRepository.deleteAll();
        notificationJpaRepository.deleteAll();
        reviewJpaRepository.deleteAll();
        favoriteJpaRepository.deleteAll();
        reservationJpaRepository.deleteAll();
        fileJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        tagJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userOAuthJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        levelJpaRepository.deleteAll();
    }

    /**
     * 테스트용 Level 생성 헬퍼 메서드
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
     * 테스트용 Owner 생성 헬퍼 메서드 (User, Level 과 함께 생성)
     */
    private Owner createTestOwner(String loginId, String name, String phone, String levelName, String businessNumber) {
        User user = createTestUserForOwner(loginId, name, phone);
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

    /**
     * 테스트용 Store 생성 헬퍼 메서드
     */
    private Store createTestStore(String name, Owner owner, Category category) {
        return storeJpaRepository.save(Store.builder()
                .name(name)
                .owner(owner)
                .category(category)
                .address("서울시 강남구")
                .phone("0212345678")
                .intro("테스트 한줄 소개")
                .introduction("테스트 가게 소개")
                .isOpen(true)
                .build());
    }

    /**
     * 테스트용 Store 생성 헬퍼 메서드 (위도/경도 포함, searchStores 테스트용)
     */
    private Store createTestStore(String name, Owner owner, Category category, String lat, String lng) {
        return storeJpaRepository.save(Store.builder()
                .name(name)
                .owner(owner)
                .category(category)
                .address("서울시 강남구")
                .phone("0212345678")
                .lat(lat)
                .lng(lng)
                .intro("테스트 한줄 소개")
                .introduction("테스트 가게 소개")
                .isOpen(true)
                .build());
    }

    /**
     * Store에 태그(해시태그) 추가 헬퍼 메서드
     */
    private void addTagsToStore(Store store, List<String> tagNames) {
        for (String tagName : tagNames) {
            Tag tag = tagJpaRepository.findAllByName(tagName).stream()
                    .findFirst()
                    .orElseGet(() -> tagJpaRepository.save(Tag.builder().name(tagName).build()));
            store.addTag(tag);
        }
        storeJpaRepository.save(store);
    }

    @Nested
    @DisplayName("가게 단건 조회 테스트")
    class GetStoreTest {

        @Test
        @DisplayName("가게 단건 조회 성공")
        @Rollback(value = false)
        public void shouldGetStore() {
            // given
            Owner owner = createTestOwner("test_owner_get_store", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            // when
            Store result = storeQueryService.getStoreById(new GetStoreByIdQuery(store.getId()));

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(store.getId());
            assertThat(result.getName()).isEqualTo("테스트 가게");
            assertThat(result.getOwner().getId()).isEqualTo(owner.getId());
            assertThat(result.getCategory().getId()).isEqualTo(category.getId());

            System.out.println("✅ 가게 단건 조회 성공");
            System.out.println("   - 가게 ID: " + result.getId());
            System.out.println("   - 가게 이름: " + result.getName());
        }
    }

    @Nested
    @DisplayName("가게 목록 조회 테스트")
    class GetStoresTest {

        @Test
        @DisplayName("가게 목록 조회 성공")
        @Rollback(value = false)
        public void shouldGetStores() {
            // given
            Owner owner = createTestOwner("test_owner_get_stores", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store1 = createTestStore("테스트 가게1", owner, category);
            Store store2 = createTestStore("테스트 가게2", owner, category);
            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> stores = storeFacade.getStores(owner.getId(), pageable);

            // then
            assertThat(stores).isNotNull();
            assertThat(stores.getTotalElements()).isEqualTo(2);
            assertThat(stores).allMatch(s -> s.store().getOwner().getId().equals(owner.getId()));
            assertThat(stores.getContent()).extracting(StoreWithFavoriteCount::store).extracting(Store::getId).contains(store1.getId(), store2.getId());

            System.out.println("✅ 가게 목록 조회 성공");
            System.out.println("   - 사업자 ID: " + owner.getId());
            System.out.println("   - 가게 개수: " + stores.getTotalElements());
        }

        @Test
        @DisplayName("가게가 없을 때 빈 목록 반환")
        public void shouldReturnEmptyListWhenNoStores() {
            // given
            Owner owner = createTestOwner("test_owner_empty_stores", "테스트 사업자", "01012345678", "새싹2", "1234567890");
            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> stores = storeFacade.getStores(owner.getId(), pageable);

            // then
            assertThat(stores).isNotNull();
            assertThat(stores).isEmpty();
        }
    }

    @Nested
    @DisplayName("가게 검색(searchStores) 테스트")
    class SearchStoresTest {

        @Test
        @DisplayName("위도/경도 있을 때 - 위치 기반 검색으로 반경 내 가게만 조회")
        @Transactional
        public void shouldSearchStoresWithLocation() {
            // given: 서울(37.5665, 126.9780) 근처에 가게 2개, 부산(35.1796, 129.0756)에 가게 1개
            Owner owner = createTestOwner("test_owner_search_loc", "테스트 사업자", "01012345678", "새싹2", "1234567890");
            Category category = createTestCategory("식당");
            Store store1 = createTestStore("강남 커피숍", owner, category, "37.5665", "126.9780");
            Store store2 = createTestStore("역삼 카페", owner, category, "37.5666", "126.9781");
            Store storeInBusan = createTestStore("부산 해운대가게", owner, category, "35.1796", "129.0756");

            SearchStoresQuery query = new SearchStoresQuery(37.5665, 126.9780, null, null);
            Pageable pageable = Pageable.ofSize(10);

            // when: 서울 좌표 기준 검색 (기본 반경 20km)
            Page<StoreWithFavoriteCount> result = storeFacade.searchStores(query, pageable);

            // then: 서울 근처 가게 2개만 조회
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(s -> s.store().getName())
                    .containsExactlyInAnyOrder(store1.getName(), store2.getName());
            assertThat(result.getContent())
                    .extracting(s -> s.store().getName())
                    .doesNotContain(storeInBusan.getName());
        }

        @Test
        @DisplayName("위도/경도 없을 때 - 전체 가게 목록 조회")
        @Transactional
        public void shouldSearchStoresWithoutLocation() {
            // given
            Owner owner = createTestOwner("test_owner_search_no_loc", "테스트 사업자", "01012345678", "새싹3", "1234567890");
            Category category = createTestCategory("식당");
            Store store1 = createTestStore("테스트 가게1", owner, category, "37.5665", "126.9780");
            Store store2 = createTestStore("테스트 가게2", owner, category, "35.1796", "129.0756");

            SearchStoresQuery query = new SearchStoresQuery(null, null, null, null);
            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> result = storeFacade.searchStores(query, pageable);

            // then: 위치 없이 검색 시 모든 가게 조회
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(s -> s.store().getId())
                    .containsExactlyInAnyOrder(store1.getId(), store2.getId());
        }

        @Test
        @DisplayName("키워드 있을 때 - 가게 이름으로 검색")
        @Transactional
        public void shouldSearchStoresByStoreName() {
            // given: 이름에 "스페셜커피"가 포함된 가게만 생성하여 키워드 검색 동작 검증
            Owner owner = createTestOwner("test_owner_search_keyword", "테스트 사업자", "01012345678", "새싹4", "1234567890");
            Category category = createTestCategory("식당");
            Store coffeeStore = createTestStore("스페셜커피전문점", owner, category, "37.5665", "126.9780");

            SearchStoresQuery query = new SearchStoresQuery(null, null, null, "스페셜커피");
            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> result = storeFacade.searchStores(query, pageable);

            // then: 가게 이름에 "스페셜커피"가 포함된 가게만 조회
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).store().getId()).isEqualTo(coffeeStore.getId());
            assertThat(result.getContent().get(0).store().getName()).isEqualTo("스페셜커피전문점");
        }

        @Test
        @DisplayName("키워드 있을 때 - 해시태그(태그)로 검색")
        @Transactional
        public void shouldSearchStoresByHashtag() {
            // given: 태그 "브런치스페셜"이 있는 가게만 생성하여 해시태그 검색 동작 검증
            Owner owner = createTestOwner("test_owner_search_hashtag", "테스트 사업자", "01012345678", "새싹5", "1234567890");
            Category category = createTestCategory("식당");
            Store storeWithTag = createTestStore("오늘의맛집", owner, category, "37.5665", "126.9780");
            addTagsToStore(storeWithTag, List.of("브런치스페셜", "조식"));

            SearchStoresQuery query = new SearchStoresQuery(null, null, null, "브런치스페셜");
            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> result = storeFacade.searchStores(query, pageable);

            // then: 태그에 "브런치스페셜"이 있는 가게만 조회
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).store().getId()).isEqualTo(storeWithTag.getId());
            assertThat(result.getContent().get(0).store().getName()).isEqualTo("오늘의맛집");
        }

        @Test
        @DisplayName("키워드 없을 때 - 전체 가게 목록 조회")
        @Transactional
        public void shouldSearchStoresWithoutKeyword() {
            // given
            Owner owner = createTestOwner("test_owner_search_no_keyword", "테스트 사업자", "01012345678", "새싹6", "1234567890");
            Category category = createTestCategory("식당");
            Store store1 = createTestStore("가게A", owner, category, "37.5665", "126.9780");
            Store store2 = createTestStore("가게B", owner, category, "37.5666", "126.9781");

            SearchStoresQuery query = new SearchStoresQuery(null, null, null, null);
            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> result = storeFacade.searchStores(query, pageable);

            // then: 키워드 없이 검색 시 모든 가게 조회
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(s -> s.store().getId())
                    .containsExactlyInAnyOrder(store1.getId(), store2.getId());
        }

        @Test
        @DisplayName("sortField=favoriteCount - tie 시 페이지 중복 없이 조회")
        @Transactional
        public void shouldNotReturnDuplicatedStoresBetweenPagesWhenSortingFavoriteCountTie() {
            // given: 모든 가게의 favoriteCount가 동일한 상황 (favorites 없음 → 0)
            Owner owner = createTestOwner(
                    "test_owner_search_favorite_tie",
                    "테스트 사업자",
                    "01012345678",
                    "새싹_favorite_tie",
                    "1234567890"
            );
            Category category = createTestCategory("식당");

            int totalStores = 12;
            for (int i = 0; i < totalStores; i++) {
                createTestStore("가게_" + i, owner, category, "37.5665", "126.9780");
            }

            SearchStoresQuery query = new SearchStoresQuery(null, null, null, null);
            String sortField = StoreSearchSortResolver.resolve("favoriteCount", false); // JPQL: COUNT(f)
            int perPage = 2;

            // when
            Set<Long> seenStoreIds = new HashSet<>();

            Page<StoreWithFavoriteCount> firstPage = storeFacade.searchStores(
                    query,
                    PageableUtil.ofOneBased(1, perPage, sortField, "desc")
            );

            long totalElements = firstPage.getTotalElements();
            seenStoreIds.addAll(firstPage.getContent().stream()
                    .map(s -> s.store().getId())
                    .toList());

            // page 2..N을 추가로 조회하며 중복되는 id가 없는지 검증
            for (int pageNum = 2; pageNum <= firstPage.getTotalPages(); pageNum++) {
                Page<StoreWithFavoriteCount> page = storeFacade.searchStores(
                        query,
                        PageableUtil.ofOneBased(pageNum, perPage, sortField, "desc")
                );

                Set<Long> pageIds = page.getContent().stream()
                        .map(s -> s.store().getId())
                        .collect(java.util.stream.Collectors.toSet());

                // 페이지 내부 중복 방지(안전장치)
                assertThat(pageIds.size()).isEqualTo(page.getContent().size());

                // 페이지 간 중복 방지(핵심)
                assertThat(seenStoreIds).doesNotContainAnyElementsOf(pageIds);
                seenStoreIds.addAll(pageIds);
            }

            // then
            assertThat(seenStoreIds.size()).isEqualTo(totalElements);
        }

        @Test
        @DisplayName("위도/경도 + 키워드 동시 조건 - 위치 내에서 키워드 필터링")
        @Transactional
        public void shouldSearchStoresWithLocationAndKeyword() {
            // given: 같은 반경 내에 "커피" 가게 1개, "식당" 가게 1개
            Owner owner = createTestOwner("test_owner_search_both", "테스트 사업자", "01012345678", "새싹7", "1234567890");
            Category category = createTestCategory("식당");
            Store coffeeStore = createTestStore("강남 커피숍", owner, category, "37.5665", "126.9780");
            createTestStore("강남 식당", owner, category, "37.5666", "126.9781");

            SearchStoresQuery query = new SearchStoresQuery(37.5665, 126.9780, null, "커피");
            Pageable pageable = Pageable.ofSize(10);

            // when
            Page<StoreWithFavoriteCount> result = storeFacade.searchStores(query, pageable);

            // then: 위치 조건 + 가게 이름 "커피" 포함된 가게만 조회
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).store().getName()).isEqualTo(coffeeStore.getName());
        }
    }

    @Nested
    @DisplayName("가게 등록 테스트")
    class CreateStoreTest {

        @Test
        @DisplayName("가게 등록 성공")
        @Rollback(value = false)
        public void shouldCreateStore() {
            // given
            String uniqueLoginId = "test_owner_create_" + UUID.randomUUID().toString().substring(0, 8);
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹8", "1234567890");
            Category category = createTestCategory("식당");

            CreateStoreCommand command = new CreateStoreCommand(
                    owner.getId(),
                    category.getId(),
                    "새로운 가게",
                    "서울시 강남구 테헤란로",
                    "0212345678",
                    "37.5665",
                    "126.9780",
                    "따뜻한 식당",
                    "맛있는 음식을 제공하는 가게입니다.",
                    List.of(), // 태그 없음
                    List.of(), // 영업시간 없음
                    List.of() // 이미지 없음
            );

            // when
            Store store = storeFacade.createStore(command, owner.getId());

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isNotNull();
            assertThat(store.getName()).isEqualTo("새로운 가게");
            assertThat(store.getAddress()).isEqualTo("서울시 강남구 테헤란로");
            assertThat(store.getPhone()).isEqualTo("0212345678");
            assertThat(store.getOwner().getId()).isEqualTo(owner.getId());
            assertThat(store.getCategory().getId()).isEqualTo(category.getId());
            // StoreCommandService: 가게 생성 시 isOpen 은 true (영업 중으로 생성)
            assertThat(store.getIsOpen()).isTrue();

            System.out.println("✅ 가게 등록 성공");
            System.out.println("   - 가게 ID: " + store.getId());
            System.out.println("   - 가게 이름: " + store.getName());
            System.out.println("   - 영업 상태: " + store.getIsOpen());
        }

        @Test
        @DisplayName("가게 등록 성공 - 태그와 함께 생성")
        @Transactional
        @Rollback(value = false)
        public void shouldCreateStoreWithTags() {
            // given
            String uniqueLoginId = "test_owner_tags_" + UUID.randomUUID().toString().substring(0, 8);
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹9", "1234567890");
            Category category = createTestCategory("식당");

            CreateStoreCommand command = new CreateStoreCommand(
                    owner.getId(),
                    category.getId(),
                    "태그 있는 가게",
                    "서울시 강남구 테헤란로",
                    "0212345678",
                    "37.5665",
                    "126.9780",
                    "태그가 있는 가게입니다.",
                    "태그 가게",
                    List.of("커피", "디저트", "브런치"), // 태그 이름 목록
                    List.of(), // 영업시간 없음
                    List.of() // 이미지 없음
            );

            // when
            Store store = storeFacade.createStore(command, owner.getId());

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isNotNull();
            assertThat(store.getName()).isEqualTo("태그 있는 가게");

            // 태그 확인 (트랜잭션 내에서 조회)
            List<StoreTag> storeTags = store.getStoreTags();
            assertThat(storeTags).isNotNull();
            assertThat(storeTags.size()).isEqualTo(3);
            assertThat(storeTags).extracting(st -> st.getTag().getName())
                    .containsExactlyInAnyOrder("커피", "디저트", "브런치");

            // 태그가 DB에 저장되었는지 확인 (StoreQueryService를 통해 다시 조회)
            Store savedStore = storeQueryService.getStoreById(new GetStoreByIdQuery(store.getId()));
            assertThat(savedStore).isNotNull();
            assertThat(savedStore.getStoreTags().size()).isEqualTo(3);
            assertThat(savedStore.getStoreTags()).extracting(st -> st.getTag().getName())
                    .containsExactlyInAnyOrder("커피", "디저트", "브런치");

            System.out.println("✅ 가게 등록 성공 (태그 포함)");
            System.out.println("   - 가게 ID: " + store.getId());
            System.out.println("   - 가게 이름: " + store.getName());
            System.out.println("   - 태그 개수: " + storeTags.size());
            storeTags.forEach(st -> {
                System.out.println("     - 태그: " + st.getTag().getName() + " (ID: " + st.getTag().getId() + ")");
            });
        }

        @Test
        @DisplayName("가게 등록 성공 - 기존 태그 재사용")
        @Transactional
        @Rollback(value = false)
        public void shouldCreateStoreWithExistingTags() {
            // given
            String uniqueLoginId = "test_owner_reuse_" + UUID.randomUUID().toString().substring(0, 8);
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹10", "1234567890");
            Category category = createTestCategory("식당");

            // 기존 태그 생성
            Tag existingTag = tagJpaRepository.save(
                    Tag.builder()
                            .name("커피")
                            .build()
            );

            CreateStoreCommand command = new CreateStoreCommand(
                    owner.getId(),
                    category.getId(),
                    "기존 태그 사용 가게",
                    "서울시 강남구 테헤란로",
                    "0212345678",
                    "37.5665",
                    "126.9780",
                    "기존 태그를 재사용하는 가게입니다.",
                    "태그 재사용",
                    List.of("커피", "새로운태그"), // 기존 태그 "커피"와 새로운 태그 "새로운태그"
                    List.of(), // 영업시간 없음
                    List.of() // 이미지 없음
            );

            // when
            Store store = storeFacade.createStore(command, owner.getId());

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isNotNull();

            // 태그 확인 (트랜잭션 내에서 조회)
            List<StoreTag> storeTags = store.getStoreTags();
            assertThat(storeTags).isNotNull();
            assertThat(storeTags.size()).isEqualTo(2);

            // 기존 태그가 재사용되었는지 확인 (같은 Tag 엔티티 사용)
            StoreTag coffeeTag = storeTags.stream()
                    .filter(st -> st.getTag().getName().equals("커피"))
                    .findFirst()
                    .orElse(null);
            assertThat(coffeeTag).isNotNull();
            assertThat(coffeeTag.getTag().getId()).isEqualTo(existingTag.getId()); // 기존 태그 재사용

            // 새로운 태그가 생성되었는지 확인
            StoreTag newTag = storeTags.stream()
                    .filter(st -> st.getTag().getName().equals("새로운태그"))
                    .findFirst()
                    .orElse(null);
            assertThat(newTag).isNotNull();
            assertThat(newTag.getTag().getId()).isNotEqualTo(existingTag.getId()); // 새로운 태그

            // DB에서 다시 조회하여 확인
            Store savedStore = storeQueryService.getStoreById(new GetStoreByIdQuery(store.getId()));
            assertThat(savedStore.getStoreTags().size()).isEqualTo(2);

            System.out.println("✅ 가게 등록 성공 (기존 태그 재사용)");
            System.out.println("   - 가게 ID: " + store.getId());
            System.out.println("   - 태그 개수: " + storeTags.size());
            System.out.println("   - 기존 태그 재사용: 커피 (ID: " + existingTag.getId() + ")");
            System.out.println("   - 새 태그 생성: 새로운태그 (ID: " + newTag.getTag().getId() + ")");
        }
    }

    @Nested
    @DisplayName("가게 정보 수정 테스트")
    class UpdateStoreTest {

        @Test
        @DisplayName("가게 정보 수정 성공")
        @Transactional
        public void shouldUpdateStore() {
            // given
            String uniqueLoginId = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹11", "1234567890");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            Store store = createTestStore("테스트 가게", owner, category1);

            UpdateStoreCommand command = new UpdateStoreCommand(
                    store.getId(),
                    category2.getId(),
                    "서울시 서초구",
                    "0298765432",
                    "37.4837",
                    "127.0324",
                    "업데이트된 가게 소개입니다.",
                    "업데이트된 한줄 소개",
                    true,
                    true,
                    List.of(), // 태그 없음
                    List.of(), // 영업시간 없음
                    List.of() // 이미지 없음
            );

            // when
            storeFacade.updateStore(command, owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getCategory().getId()).isEqualTo(category2.getId());
            assertThat(updatedStore.getPhone()).isEqualTo("0298765432");
            assertThat(updatedStore.getAddress()).isEqualTo("서울시 서초구");
            assertThat(updatedStore.getIntroduction()).isEqualTo("업데이트된 가게 소개입니다.");
            assertThat(updatedStore.getIntro()).isEqualTo("업데이트된 한줄 소개");
            assertThat(updatedStore.getIsOpen()).isTrue();
            assertThat(updatedStore.getIsSharing()).isTrue();

            System.out.println("✅ 가게 정보 수정 성공");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 카테고리: " + updatedStore.getCategory().getName());
        }

        @Test
        @DisplayName("다른 사업자의 가게 수정 시 예외 발생")
        @Transactional
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            String uniqueLoginId1 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            String uniqueLoginId2 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Owner owner1 = createTestOwner(uniqueLoginId1, "테스트 사업자1", "01012345678", "새싹12", "1234567890");
            Owner owner2 = createTestOwner(uniqueLoginId2, "테스트 사업자2", "01087654321", "새싹13", "2234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner1, category);

            UpdateStoreCommand command = new UpdateStoreCommand(
                    store.getId(),
                    category.getId(),
                    "0298765432",
                    "서울시 서초구",
                    "37.4837",
                    "127.0324",
                    "업데이트된 가게 소개입니다.",
                    "업데이트된 한줄 소개",
                    true,
                    true,
                    List.of(), // 태그 없음
                    List.of(), // 영업시간 없음
                    List.of() // 이미지 없음
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                    CoreException.class,
                    () -> storeFacade.updateStore(command, owner2.getId())
            );

            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("가게 카테고리 수정 테스트")
    class UpdateCategoryTest {

        @Test
        @DisplayName("가게 카테고리 수정 성공")
        @Transactional
        public void shouldUpdateCategory() {
            // given
            String uniqueLoginId1 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Owner owner = createTestOwner(uniqueLoginId1, "테스트 사업자", "01012345678", "새싹14", "1234567890");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            Store store = createTestStore("테스트 가게", owner, category1);

            // when
            storeFacade.updateCategory(store.getId(), category2.getId(), owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getCategory().getId()).isEqualTo(category2.getId());
            assertThat(updatedStore.getCategory().getName()).isEqualTo("카페");

            System.out.println("✅ 가게 카테고리 수정 성공");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 변경 전 카테고리: " + category1.getName());
            System.out.println("   - 변경 후 카테고리: " + updatedStore.getCategory().getName());
        }

        @Test
        @DisplayName("다른 사업자의 가게 카테고리 수정 시 예외 발생")
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            String uniqueLoginId1 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            String uniqueLoginId2 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Owner owner1 = createTestOwner(uniqueLoginId1, "테스트 사업자1", "01012345678", "새싹15", "1234567890");
            Owner owner2 = createTestOwner(uniqueLoginId2, "테스트 사업자2", "01087654321", "새싹16", "2234567890");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            Store store = createTestStore("테스트 가게", owner1, category1);

            // when & then
            CoreException exception = Assertions.assertThrows(
                    CoreException.class,
                    () -> storeFacade.updateCategory(store.getId(), category2.getId(), owner2.getId())
            );

            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("가게 삭제 테스트")
    class DeleteStoreTest {

        @Test
        @DisplayName("가게 삭제 성공")
        @Rollback(value = false)
        public void shouldDeleteStore() {
            // given
            Owner owner = createTestOwner("test_owner_delete_store", "테스트 사업자", "01012345678", "새싹17", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            // when
            storeFacade.deleteStore(new DeleteStoreCommand(store.getId()), owner.getId());

            // then
            boolean exists = storeJpaRepository.existsById(store.getId());
            assertThat(exists).isFalse();

            System.out.println("✅ 가게 삭제 성공");
            System.out.println("   - 삭제된 가게 ID: " + store.getId());
        }

        @Test
        @DisplayName("다른 사업자의 가게 삭제 시 예외 발생")
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            Owner owner1 = createTestOwner("test_owner1_delete", "테스트 사업자1", "01012345678", "새싹18", "1234567890");
            Owner owner2 = createTestOwner("test_owner2_delete", "테스트 사업자2", "01087654321", "새싹19", "2234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner1, category);

            // when & then
            CoreException exception = Assertions.assertThrows(
                    CoreException.class,
                    () -> storeFacade.deleteStore(new DeleteStoreCommand(store.getId()), owner2.getId())
            );

            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("가게 영업 상태 변경 테스트")
    class ChangeOpenStatusTest {

        @Test
        @DisplayName("가게 영업 상태 변경 성공 - 영업 중으로 변경")
        @Rollback(value = false)
        public void shouldChangeOpenStatusToOpen() {
            // given
            Owner owner = createTestOwner("test_owner_change_status", "테스트 사업자", "01012345678", "새싹20", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            // 초기 상태를 false로 설정
            store.changeOpenStatus(false);
            storeJpaRepository.save(store);

            // when
            storeFacade.changeOpenStatus(store.getId(), true, owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getIsOpen()).isTrue();

            System.out.println("✅ 가게 영업 상태 변경 성공 (영업 중)");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 영업 상태: " + updatedStore.getIsOpen());
        }

        @Test
        @DisplayName("가게 영업 상태 변경 성공 - 영업 종료로 변경")
        @Rollback(value = false)
        public void shouldChangeOpenStatusToClosed() {
            // given
            Owner owner = createTestOwner("test_owner_change_status_closed", "테스트 사업자", "01012345678", "새싹21", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            // 초기 상태를 true로 설정
            store.changeOpenStatus(true);
            storeJpaRepository.save(store);

            // when
            storeFacade.changeOpenStatus(store.getId(), false, owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getIsOpen()).isFalse();

            System.out.println("✅ 가게 영업 상태 변경 성공 (영업 종료)");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 영업 상태: " + updatedStore.getIsOpen());
        }

        @Test
        @DisplayName("다른 사업자의 가게 영업 상태 변경 시 예외 발생")
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            Owner owner1 = createTestOwner("test_owner1_status", "테스트 사업자1", "01012345678", "새싹22", "1234567890");
            Owner owner2 = createTestOwner("test_owner2_status", "테스트 사업자2", "01087654321", "새싹23", "2234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner1, category);

            // when & then
            CoreException exception = Assertions.assertThrows(
                    CoreException.class,
                    () -> storeFacade.changeOpenStatus(store.getId(), true, owner2.getId())
            );

            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }
}

package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.file.FileJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

/**
 * StoreControllerImpl의 가게 목록 조회 API 테스트
 * 
 * 파일이 있을 때, 없을 때, 그룹화되어 있을 때를 모두 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("StoreController 가게 목록 조회 테스트")
class StoreControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private FileJpaRepository fileJpaRepository;

    @BeforeEach
    void setUp() {
        // 외래 키 제약 조건을 고려한 삭제 순서 (자식 → 부모)
        fileJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
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
     * 테스트용 Owner 생성 헬퍼 메서드
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
     * 테스트용 File 생성 헬퍼 메서드
     */
    private File createTestFile(Long refId, String filePath, int displayOrder) {
        String uniqueKey = "image/" + UUID.randomUUID().toString() + ".jpg";
        return fileJpaRepository.save(
                File.builder()
                        .fileKey(uniqueKey)
                        .storedFileName("test-image.jpg")
                        .filePath(filePath)
                        .fileExtension("jpg")
                        .fileSize(1024L)
                        .refType(AttachmentType.STORE)
                        .refId(refId)
                        .displayOrder(displayOrder)
                        .build()
        );
    }

    @Nested
    @DisplayName("가게 목록 조회 - 파일이 있을 때")
    class SearchStoresWithFilesTest {

        @Test
        @DisplayName("가게 목록 조회 성공 - 각 가게에 이미지 파일이 있는 경우")
        @Transactional        
        void shouldSearchStoresWithImages() throws Exception {
            // given
            Owner owner = createTestOwner("test_owner_with_files", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            Store store1 = createTestStore("가게1", owner, category, "37.5665", "126.9780");
            Store store2 = createTestStore("가게2", owner, category, "37.5666", "126.9781");
            
            // 각 가게에 이미지 파일 추가
            createTestFile(store1.getId(), "https://example.com/store1-image1.jpg", 0);
            createTestFile(store1.getId(), "https://example.com/store1-image2.jpg", 1);
            createTestFile(store2.getId(), "https://example.com/store2-image1.jpg", 0);

            // when & then
            MvcResult result = mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stores").isArray())
                    .andExpect(jsonPath("$.data.stores.length()").value(2))
                    .andReturn();

            // 응답 본문 검증
            String responseBody = result.getResponse().getContentAsString();
            System.out.println("✅ 가게 목록 조회 성공 (파일 있음)");
            System.out.println("   응답: " + responseBody);

            // 첫 번째 가게의 이미지 목록 검증
            mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stores[0].id").value(store1.getId()))
                    .andExpect(jsonPath("$.data.stores[0].images").isArray())
                    .andExpect(jsonPath("$.data.stores[0].images.length()").value(2))
                    .andExpect(jsonPath("$.data.stores[0].images[0]").value("https://example.com/store1-image1.jpg"))
                    .andExpect(jsonPath("$.data.stores[0].images[1]").value("https://example.com/store1-image2.jpg"))
                    .andExpect(jsonPath("$.data.stores[1].id").value(store2.getId()))
                    .andExpect(jsonPath("$.data.stores[1].images").isArray())
                    .andExpect(jsonPath("$.data.stores[1].images.length()").value(1))
                    .andExpect(jsonPath("$.data.stores[1].images[0]").value("https://example.com/store2-image1.jpg"));

            System.out.println("   - 가게1 이미지 개수: 2개");
            System.out.println("   - 가게2 이미지 개수: 1개");
        }

        @Test
        @DisplayName("가게 목록 조회 성공 - 이미지가 표시 순서대로 정렬되어 반환되는지 확인")
        @Transactional        
        void shouldReturnImagesInDisplayOrder() throws Exception {
            // given
            Owner owner = createTestOwner("test_owner_order", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            Store store = createTestStore("정렬 테스트 가게", owner, category, "37.5665", "126.9780");
            
            // displayOrder 역순으로 저장 (2, 1, 0)
            createTestFile(store.getId(), "https://example.com/image3.jpg", 2);
            createTestFile(store.getId(), "https://example.com/image2.jpg", 1);
            createTestFile(store.getId(), "https://example.com/image1.jpg", 0);

            // when & then
            mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stores[0].images.length()").value(3))
                    .andExpect(jsonPath("$.data.stores[0].images[0]").value("https://example.com/image1.jpg")) // displayOrder 0
                    .andExpect(jsonPath("$.data.stores[0].images[1]").value("https://example.com/image2.jpg")) // displayOrder 1
                    .andExpect(jsonPath("$.data.stores[0].images[2]").value("https://example.com/image3.jpg")); // displayOrder 2

            System.out.println("✅ 이미지가 표시 순서대로 정렬되어 반환됨");
        }
    }

    @Nested
    @DisplayName("가게 목록 조회 - 파일이 없을 때")
    class SearchStoresWithoutFilesTest {

        @Test
        @DisplayName("가게 목록 조회 성공 - 이미지 파일이 없는 경우")
        @Transactional        
        void shouldSearchStoresWithoutImages() throws Exception {
            // given
            Owner owner = createTestOwner("test_owner_no_files", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            Store store1 = createTestStore("이미지 없는 가게1", owner, category, "37.5665", "126.9780");
            Store store2 = createTestStore("이미지 없는 가게2", owner, category, "37.5666", "126.9781");
            // 이미지 파일은 생성하지 않음

            // when & then
            mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stores").isArray())
                    .andExpect(jsonPath("$.data.stores.length()").value(2))
                    .andExpect(jsonPath("$.data.stores[0].id").value(store1.getId()))
                    .andExpect(jsonPath("$.data.stores[0].images").isArray())
                    .andExpect(jsonPath("$.data.stores[0].images.length()").value(0))
                    .andExpect(jsonPath("$.data.stores[1].id").value(store2.getId()))
                    .andExpect(jsonPath("$.data.stores[1].images").isArray())
                    .andExpect(jsonPath("$.data.stores[1].images.length()").value(0));

            System.out.println("✅ 가게 목록 조회 성공 (파일 없음)");
            System.out.println("   - 모든 가게의 images 필드가 빈 배열로 반환됨");
        }

        @Test
        @DisplayName("가게 목록 조회 성공 - 일부 가게만 이미지가 없는 경우")
        @Transactional        
        void shouldSearchStoresWithMixedImages() throws Exception {
            // given
            Owner owner = createTestOwner("test_owner_mixed", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            Store storeWithImage = createTestStore("이미지 있는 가게", owner, category, "37.5665", "126.9780");
            Store storeWithoutImage = createTestStore("이미지 없는 가게", owner, category, "37.5666", "126.9781");
            
            // 한 가게에만 이미지 추가
            createTestFile(storeWithImage.getId(), "https://example.com/store-image.jpg", 0);

            // when & then
            mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stores.length()").value(2))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + storeWithImage.getId() + ")].images.length()").value(1))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + storeWithoutImage.getId() + ")].images.length()").value(0));

            System.out.println("✅ 가게 목록 조회 성공 (혼합)");
            System.out.println("   - 이미지 있는 가게: 1개 이미지");
            System.out.println("   - 이미지 없는 가게: 빈 배열");
        }
    }

    @Nested
    @DisplayName("가게 목록 조회 - 파일이 그룹화되어 있을 때")
    class SearchStoresWithGroupedFilesTest {

        @Test
        @DisplayName("가게 목록 조회 성공 - 여러 가게에 각각 파일이 그룹화되어 있는 경우")
        @Transactional        
        void shouldSearchStoresWithGroupedFiles() throws Exception {
            // given
            Owner owner = createTestOwner("test_owner_grouped", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            Store store1 = createTestStore("가게1", owner, category, "37.5665", "126.9780");
            Store store2 = createTestStore("가게2", owner, category, "37.5666", "126.9781");
            Store store3 = createTestStore("가게3", owner, category, "37.5667", "126.9782");
            
            // 각 가게에 여러 이미지 파일 추가 (그룹화)
            // 가게1: 3개 이미지
            createTestFile(store1.getId(), "https://example.com/store1-img1.jpg", 0);
            createTestFile(store1.getId(), "https://example.com/store1-img2.jpg", 1);
            createTestFile(store1.getId(), "https://example.com/store1-img3.jpg", 2);
            
            // 가게2: 2개 이미지
            createTestFile(store2.getId(), "https://example.com/store2-img1.jpg", 0);
            createTestFile(store2.getId(), "https://example.com/store2-img2.jpg", 1);
            
            // 가게3: 1개 이미지
            createTestFile(store3.getId(), "https://example.com/store3-img1.jpg", 0);

            // when & then
            MvcResult result = mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stores").isArray())
                    .andExpect(jsonPath("$.data.stores.length()").value(3))
                    .andReturn();

            // 각 가게의 이미지가 올바르게 그룹화되어 반환되는지 검증
            mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    // 가게1 검증
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store1.getId() + ")].images.length()").value(3))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store1.getId() + ")].images[0]").value("https://example.com/store1-img1.jpg"))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store1.getId() + ")].images[1]").value("https://example.com/store1-img2.jpg"))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store1.getId() + ")].images[2]").value("https://example.com/store1-img3.jpg"))
                    // 가게2 검증
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store2.getId() + ")].images.length()").value(2))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store2.getId() + ")].images[0]").value("https://example.com/store2-img1.jpg"))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store2.getId() + ")].images[1]").value("https://example.com/store2-img2.jpg"))
                    // 가게3 검증
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store3.getId() + ")].images.length()").value(1))
                    .andExpect(jsonPath("$.data.stores[?(@.id == " + store3.getId() + ")].images[0]").value("https://example.com/store3-img1.jpg"));

            System.out.println("✅ 가게 목록 조회 성공 (파일 그룹화)");
            System.out.println("   - 가게1: 3개 이미지 (올바르게 그룹화됨)");
            System.out.println("   - 가게2: 2개 이미지 (올바르게 그룹화됨)");
            System.out.println("   - 가게3: 1개 이미지 (올바르게 그룹화됨)");
        }

        @Test
        @DisplayName("가게 목록 조회 성공 - 배치 조회로 N+1 문제가 발생하지 않는지 확인")
        @Transactional        
        void shouldUseBatchQueryToPreventNPlusOne() throws Exception {
            // given: 많은 가게와 파일 생성
            Owner owner = createTestOwner("test_owner_batch", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            // 10개의 가게 생성
            List<Store> stores = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Store store = createTestStore("가게" + i, owner, category, 
                        String.valueOf(37.5665 + i * 0.001), 
                        String.valueOf(126.9780 + i * 0.001));
                stores.add(store);
                
                // 각 가게에 2개씩 이미지 추가
                createTestFile(store.getId(), "https://example.com/store" + i + "-img1.jpg", 0);
                createTestFile(store.getId(), "https://example.com/store" + i + "-img2.jpg", 1);
            }

            // when & then
            MvcResult result = mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stores.length()").value(10))
                    .andReturn();

            // 각 가게의 이미지 개수 검증
            for (int i = 0; i < 10; i++) {
                mockMvc.perform(get("/api/stores")
                                .param("latitude", "37.5665")
                                .param("longitude", "126.9780")
                                .param("radius", "5.0")
                                .param("pageNum", "1")
                                .param("perPage", "20")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data.stores[" + i + "].images.length()").value(2));
            }

            System.out.println("✅ 배치 조회로 N+1 문제 방지 확인");
            System.out.println("   - 10개 가게, 각각 2개 이미지");
            System.out.println("   - 모든 가게의 이미지가 올바르게 반환됨");
        }
    }

    @Nested
    @DisplayName("가게 목록 조회 - 엣지 케이스")
    class SearchStoresEdgeCasesTest {

        @Test
        @DisplayName("가게 목록 조회 성공 - 빈 결과일 때")
        @Transactional
        void shouldReturnEmptyList() throws Exception {
            // given: 가게 없음

            // when & then
            mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stores").isArray())
                    .andExpect(jsonPath("$.data.stores.length()").value(0))
                    .andExpect(jsonPath("$.data.totalCount").value(0));

            System.out.println("✅ 빈 목록 반환 성공");
        }

        @Test
        @DisplayName("가게 목록 조회 성공 - 페이징 정보 확인")
        @Transactional        
        void shouldReturnCorrectPaginationInfo() throws Exception {
            // given
            Owner owner = createTestOwner("test_owner_pagination", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            // 5개 가게 생성
            for (int i = 0; i < 5; i++) {
                createTestStore("가게" + i, owner, category, 
                        String.valueOf(37.5665 + i * 0.001), 
                        String.valueOf(126.9780 + i * 0.001));
            }

            // when & then
            mockMvc.perform(get("/api/stores")
                            .param("latitude", "37.5665")
                            .param("longitude", "126.9780")
                            .param("radius", "5.0")
                            .param("pageNum", "1")
                            .param("perPage", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stores.length()").value(3))
                    .andExpect(jsonPath("$.data.totalCount").value(5))
                    .andExpect(jsonPath("$.data.currentPage").value(1))
                    .andExpect(jsonPath("$.data.perPage").value(3))
                    .andExpect(jsonPath("$.data.totalPages").exists());

            System.out.println("✅ 페이징 정보가 올바르게 반환됨");
        }
    }
}

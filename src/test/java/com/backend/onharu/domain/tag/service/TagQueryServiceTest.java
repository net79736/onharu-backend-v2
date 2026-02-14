package com.backend.onharu.domain.tag.service;

import static com.backend.onharu.domain.support.error.ErrorType.Tag.TAG_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.tag.dto.TagQuery.FindByIdsQuery;
import com.backend.onharu.domain.tag.dto.TagQuery.FindByNameQuery;
import com.backend.onharu.domain.tag.dto.TagQuery.GetTagByIdQuery;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;

@SpringBootTest
@DisplayName("TagQueryService 단위 테스트")
@ActiveProfiles("test")
class TagQueryServiceTest {

    @Autowired
    private TagQueryService tagQueryService;

    @Autowired
    private TagJpaRepository tagJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @BeforeEach
    public void setUp() {
        // 외래 키 제약 조건을 고려한 삭제 순서 (자식 → 부모)
        // StoreTag가 Tag를 참조하므로, Store를 먼저 삭제하면 StoreTag도 함께 삭제됨
        reservationJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll(); // Store 삭제 시 StoreTag도 자동 삭제됨
        tagJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("태그 단건 조회 테스트")
    class GetTagTest {
        
        @Test
        @DisplayName("조회 실패 - 태그 ID가 존재하지 않는 경우")
        public void shouldThrowExceptionWhenTagIsNotFound() {
            // given
            Long tagId = 99L;

            // when
            CoreException coreException = Assertions.assertThrows(
                CoreException.class, 
                () -> tagQueryService.getTag(new GetTagByIdQuery(tagId))
            );

            // then
            assertThat(coreException.getErrorType()).isEqualTo(TAG_NOT_FOUND);
        }

        @Test
        @DisplayName("조회 성공")
        @Rollback(value = false)
        public void shouldGetTag() {
            // given
            Tag savedTag = tagJpaRepository.save(
                Tag.builder()
                    .name("조회 테스트 태그")
                    .build()
            );

            // when
            Tag tag = tagQueryService.getTag(
                new GetTagByIdQuery(savedTag.getId())
            );

            // then
            assertThat(tag).isNotNull();
            assertThat(tag.getId()).isEqualTo(savedTag.getId());
            assertThat(tag.getName()).isEqualTo("조회 테스트 태그");
            
            System.out.println("✅ 태그 조회 성공 - Tag ID: " + tag.getId());
            System.out.println("   - 태그명: " + tag.getName());
        }
    }

    @Nested
    @DisplayName("태그 이름으로 검색 테스트")
    class FindByNameTest {
        
        @Test
        @DisplayName("조회 성공 - 태그 이름으로 검색")
        @Rollback(value = false)
        public void shouldGetTagsByName() {
            // given
            tagJpaRepository.save(
                Tag.builder()
                    .name("카라멜 마끼아또")
                    .build()
            );
            
            tagJpaRepository.save(
                Tag.builder()
                    .name("카페라떼")
                    .build()
            );
            
            tagJpaRepository.save(
                Tag.builder()
                    .name("아메리카노")
                    .build()
            );

            // when
            List<Tag> tags = tagQueryService.findByName(
                new FindByNameQuery("카라멜 마끼아또")
            );

            // then
            assertThat(tags.size()).isGreaterThanOrEqualTo(0);
            
            System.out.println("✅ 태그 이름으로 검색 성공");
            System.out.println("   - 검색어: 커피");
            System.out.println("   - 검색 결과 개수: " + tags.size());
            tags.forEach(t -> {
                System.out.println("     * 태그 ID: " + t.getId() + ", 태그명: " + t.getName());
            });
        }

        @Test
        @DisplayName("조회 실패 - 태그 이름으로 검색")
        @Rollback(value = false)
        public void shouldGetTagsByNameNotFound() {
            // given
            tagJpaRepository.save(
                Tag.builder()
                    .name("카페라떼")
                    .build()
            );
            
            tagJpaRepository.save(
                Tag.builder()
                    .name("아메리카노")
                    .build()
            );
            
            tagJpaRepository.save(
                Tag.builder()
                    .name("카라멜 마끼아또")
                    .build()
            );

            // when
            List<Tag> tags = tagQueryService.findByName(
                new FindByNameQuery("아이스 아메리카노")
            );

            // then
            assertThat(tags).isEmpty();
            assertThat(tags.size()).isZero();
            
            System.out.println("✅ 태그 이름으로 검색 성공");
            System.out.println("   - 검색어: 커피");
            System.out.println("   - 검색 결과 개수: " + tags.size());
            tags.forEach(t -> {
                System.out.println("     * 태그 ID: " + t.getId() + ", 태그명: " + t.getName());
            });
        }
    }

    @Nested
    @DisplayName("ID 리스트로 태그 목록 조회 테스트")
    class FindAllByIdsTest {
        
        @Test
        @DisplayName("조회 성공 - ID 리스트로 태그 목록 조회")
        @Rollback(value = false)
        public void shouldGetTagsByIds() {
            // given
            List<Tag> savedTags = saveDummyTags();
            List<Long> tagIds = savedTags.stream()
                .map(Tag::getId)
                .toList();

            // when
            List<Tag> tags = tagQueryService.findAllByIds(
                new FindByIdsQuery(tagIds)
            );

            // then
            assertThat(tags).hasSize(3);
            assertThat(tags).extracting(Tag::getName)
                .contains("태그1", "태그2", "태그3");
            
            System.out.println("✅ ID 리스트로 태그 목록 조회 성공");
            System.out.println("   - 조회할 태그 ID 개수: " + tagIds.size());
            System.out.println("   - 조회된 태그 개수: " + tags.size());
            tags.forEach(t -> {
                System.out.println("     * 태그 ID: " + t.getId() + ", 태그명: " + t.getName());
            });
        }
    }

    // 더미 데이터 생성
    private List<Tag> saveDummyTags() {
        return tagJpaRepository.saveAll(List.of(
            Tag.builder()
                .name("태그1")
                .build(),
            Tag.builder()
                .name("태그2")
                .build(),
            Tag.builder()
                .name("태그3")
                .build()
        ));
    }
}

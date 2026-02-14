package com.backend.onharu.domain.tag.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.tag.dto.TagCommand.CreateTagCommand;
import com.backend.onharu.domain.tag.dto.TagCommand.DeleteTagCommand;
import com.backend.onharu.domain.tag.dto.TagCommand.UpdateTagCommand;
import com.backend.onharu.domain.tag.dto.TagQuery.GetTagByIdQuery;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;

@SpringBootTest
@DisplayName("TagCommandService 단위 테스트")
@ActiveProfiles("test")
class TagCommandServiceTest {

    @Autowired
    private TagCommandService tagCommandService;

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
    @DisplayName("태그 생성 테스트")
    class CreateTagTest {
        
        @Test
        @DisplayName("태그 생성 성공")
        @Rollback(value = false)
        public void shouldCreateTag() {
            // given
            String tagName = "커피";

            // when
            Tag tag = tagCommandService.createTag(new CreateTagCommand(tagName));

            // then
            assertThat(tag).isNotNull();
            assertThat(tag.getId()).isNotNull();
            assertThat(tag.getName()).isEqualTo(tagName);
            
            // DB에 저장되었는지 확인
            Tag savedTag = tagQueryService.getTag(new GetTagByIdQuery(tag.getId()));
            assertThat(savedTag).isNotNull();
            assertThat(savedTag.getName()).isEqualTo(tagName);
            
            System.out.println("✅ 태그 생성 성공 - Tag ID: " + tag.getId());
            System.out.println("   - 태그명: " + tag.getName());
        }
    }

    @Nested
    @DisplayName("태그 수정 테스트")
    class UpdateTagTest {
        
        @Test
        @DisplayName("태그 수정 성공")
        @Rollback(value = false)
        public void shouldUpdateTag() {
            // given
            Tag savedTag = tagJpaRepository.save(
                Tag.builder()
                    .name("기존 태그")
                    .build()
            );
            
            String newTagName = "수정된 태그";

            // when
            tagCommandService.updateTag(
                new UpdateTagCommand(savedTag.getId(), newTagName)
            );

            // then
            Tag updatedTag = tagQueryService.getTag(
                new GetTagByIdQuery(savedTag.getId())
            );
            assertThat(updatedTag.getName()).isEqualTo(newTagName);
            
            System.out.println("✅ 태그 수정 성공 - Tag ID: " + updatedTag.getId());
            System.out.println("   - 수정 전 태그명: 기존 태그");
            System.out.println("   - 수정 후 태그명: " + updatedTag.getName());
        }
    }

    @Nested
    @DisplayName("태그 삭제 테스트")
    class DeleteTagTest {
        
        @Test
        @DisplayName("태그 삭제 성공")
        @Rollback(value = false)
        public void shouldDeleteTag() {
            // given
            Tag savedTag = tagJpaRepository.save(
                Tag.builder()
                    .name("삭제할 태그")
                    .build()
            );

            // when
            tagCommandService.deleteTag(
                new DeleteTagCommand(savedTag.getId())
            );

            // then
            // 삭제되었는지 확인 (조회 시 예외 발생)
            boolean deleted = !tagJpaRepository.existsById(savedTag.getId());
            assertThat(deleted).isTrue();
            
            System.out.println("✅ 태그 삭제 성공 - Tag ID: " + savedTag.getId());
            System.out.println("   - 삭제 확인: " + deleted);
        }
    }
}

package com.backend.onharu.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefQuery;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.service.FileQueryService;
import com.backend.onharu.infra.db.file.FileJpaRepository;

@SpringBootTest
@DisplayName("FileFacade 단위 테스트")
class FileFacadeTest {

    @Autowired
    private FileFacade fileFacade;

    @Autowired
    private FileQueryService fileQueryService;

    @Autowired
    private FileJpaRepository fileJpaRepository;

    @BeforeEach
    void setUp() {
        fileJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("registerFiles 테스트")
    class RegisterFilesTest {

        @Test
        @DisplayName("images가 null이면 아무 것도 등록하지 않음")
        void shouldDoNothingWhenImagesIsNull() {
            // when
            fileFacade.registerFiles(AttachmentType.STORE, 1L, null);

            // then
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, 1L));
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("images가 비어 있으면 아무 것도 등록하지 않음")
        void shouldDoNothingWhenImagesIsEmpty() {
            // when
            fileFacade.registerFiles(AttachmentType.STORE, 1L, List.of());

            // then
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, 1L));
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("이미지 목록 등록 성공 - displayOrder 순서대로 저장됨")
        @Rollback(value = false)
        void shouldRegisterFilesInOrder() {
            // given
            Long refId = 100L;
            List<ImageMetadata> images = List.of(
                    new ImageMetadata("image/uuid-a.jpg", "https://example.com/bucket/image/uuid-a.jpg", 0),
                    new ImageMetadata("image/uuid-b.png", "https://example.com/bucket/image/uuid-b.png", 1)
            );

            // when
            fileFacade.registerFiles(AttachmentType.STORE, refId, images);

            // then
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, refId));
            assertThat(list).hasSize(2);
            assertThat(list.get(0).getFileKey()).isEqualTo("image/uuid-a.jpg");
            assertThat(list.get(0).getDisplayOrder()).isZero();
            assertThat(list.get(1).getFileKey()).isEqualTo("image/uuid-b.png");
            assertThat(list.get(1).getDisplayOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("replaceFiles 테스트")
    class ReplaceFilesTest {

        @Test
        @DisplayName("기존 첨부 없을 때 새 목록 등록 - 등록만 수행")
        @Rollback(value = false)
        void shouldRegisterWhenNoExistingFiles() {
            // given
            Long refId = 200L;
            List<ImageMetadata> images = List.of(
                    new ImageMetadata("image/new.jpg", "https://example.com/bucket/image/new.jpg", 0)
            );

            // when
            fileFacade.replaceFiles(AttachmentType.STORE, refId, images);

            // then
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, refId));
            assertThat(list).hasSize(1);
            assertThat(list.get(0).getFileKey()).isEqualTo("image/new.jpg");
        }

        @Test
        @DisplayName("기존 첨부 삭제 후 새 목록으로 교체")
        @Rollback(value = false)
        void shouldReplaceExistingFilesWithNewList() {
            // given: 기존 파일 1개 등록
            Long refId = 300L;
            fileFacade.registerFiles(AttachmentType.STORE, refId, List.of(
                    new ImageMetadata("image/old.jpg", "https://example.com/bucket/image/old.jpg", 0)
            ));
            assertThat(fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, refId))).hasSize(1);

            // when: 다른 목록으로 교체
            List<ImageMetadata> newImages = List.of(
                    new ImageMetadata("image/new1.jpg", "https://example.com/bucket/image/new1.jpg", 0),
                    new ImageMetadata("image/new2.jpg", "https://example.com/bucket/image/new2.jpg", 1)
            );
            fileFacade.replaceFiles(AttachmentType.STORE, refId, newImages);

            // then: 기존 1개 삭제, 새 2개만 존재
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, refId));
            assertThat(list).hasSize(2);
            assertThat(list.get(0).getFileKey()).isEqualTo("image/new1.jpg");
            assertThat(list.get(1).getFileKey()).isEqualTo("image/new2.jpg");
        }

        @Test
        @DisplayName("빈 목록으로 교체 시 기존만 삭제하고 등록 없음")
        @Rollback(value = false)
        void shouldDeleteOnlyWhenReplacingWithEmptyList() {
            // given
            Long refId = 400L;
            fileFacade.registerFiles(AttachmentType.STORE, refId, List.of(
                    new ImageMetadata("image/only.jpg", "https://example.com/bucket/image/only.jpg", 0)
            ));

            // when: 빈 목록으로 교체 (replaceFiles는 images가 null이 아닐 때만 호출한다고 가정; 빈 목록 전달)
            fileFacade.replaceFiles(AttachmentType.STORE, refId, List.of());

            // then
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, refId));
            assertThat(list).isEmpty();
        }
    }
}

package com.backend.onharu.domain.file.service;

import static com.backend.onharu.domain.support.error.ErrorType.FileOperation.FILE_NOT_FOUND;
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

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.dto.FileQuery.GetByFileKeyQuery;
import com.backend.onharu.domain.file.dto.FileQuery.GetByIdQuery;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefQuery;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.file.FileJpaRepository;

@SpringBootTest
@DisplayName("FileQueryService 단위 테스트")
@ActiveProfiles("test")
class FileQueryServiceTest {

    @Autowired
    private FileQueryService fileQueryService;

    @Autowired
    private FileJpaRepository fileJpaRepository;

    @BeforeEach
    void setUp() {
        fileJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("listByRef 테스트")
    class ListByRefTest {

        @Test
        @DisplayName("첨부가 없으면 빈 목록 반환")
        void shouldReturnEmptyListWhenNoFiles() {
            // when
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, 1L));

            // then
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("표시 순서(displayOrder) 오름차순으로 반환")
        @Rollback(value = false)
        void shouldReturnFilesOrderedByDisplayOrder() {
            // given: displayOrder 역순으로 저장
            Long refId = 100L;
            fileJpaRepository.save(
                    File.builder()
                            .fileKey("image/second.jpg")
                            .storedFileName("second.jpg")
                            .filePath("https://example.com/second.jpg")
                            .refType(AttachmentType.STORE)
                            .refId(refId)
                            .displayOrder(1)
                            .build()
            );
            fileJpaRepository.save(
                    File.builder()
                            .fileKey("image/first.jpg")
                            .storedFileName("first.jpg")
                            .filePath("https://example.com/first.jpg")
                            .refType(AttachmentType.STORE)
                            .refId(refId)
                            .displayOrder(0)
                            .build()
            );

            // when
            List<File> list = fileQueryService.listByRef(new ListByRefQuery(AttachmentType.STORE, refId));

            // then
            assertThat(list).hasSize(2);
            assertThat(list.get(0).getDisplayOrder()).isEqualTo(0);
            assertThat(list.get(0).getFileKey()).isEqualTo("image/first.jpg");
            assertThat(list.get(1).getDisplayOrder()).isEqualTo(1);
            assertThat(list.get(1).getFileKey()).isEqualTo("image/second.jpg");
        }
    }

    @Nested
    @DisplayName("getById 테스트")
    class GetByIdTest {

        @Test
        @DisplayName("조회 실패 - 존재하지 않는 ID")
        void shouldThrowExceptionWhenNotFound() {
            // given
            Long notExistId = 99999L;

            // when
            CoreException ex = Assertions.assertThrows(
                    CoreException.class,
                    () -> fileQueryService.getById(new GetByIdQuery(notExistId))
            );

            // then
            assertThat(ex.getErrorType()).isEqualTo(FILE_NOT_FOUND);
        }

        @Test
        @DisplayName("조회 성공")
        @Rollback(value = false)
        void shouldGetFileById() {
            // given
            File saved = fileJpaRepository.save(
                    File.builder()
                            .fileKey("image/saved.jpg")
                            .storedFileName("saved.jpg")
                            .filePath("https://example.com/saved.jpg")
                            .refType(AttachmentType.STORE)
                            .refId(1L)
                            .displayOrder(0)
                            .build()
            );

            // when
            File file = fileQueryService.getById(new GetByIdQuery(saved.getId()));

            // then
            assertThat(file).isNotNull();
            assertThat(file.getId()).isEqualTo(saved.getId());
            assertThat(file.getFileKey()).isEqualTo("image/saved.jpg");
        }
    }

    @Nested
    @DisplayName("getByFileKey 테스트")
    class GetByFileKeyTest {

        @Test
        @DisplayName("조회 실패 - 존재하지 않는 fileKey")
        void shouldThrowExceptionWhenFileKeyNotFound() {
            // given
            String notExistKey = "image/not-exist.jpg";

            // when
            CoreException ex = Assertions.assertThrows(
                    CoreException.class,
                    () -> fileQueryService.getByFileKey(new GetByFileKeyQuery(notExistKey))
            );

            // then
            assertThat(ex.getErrorType()).isEqualTo(FILE_NOT_FOUND);
        }

        @Test
        @DisplayName("조회 성공")
        @Rollback(value = false)
        void shouldGetFileByFileKey() {
            // given
            String fileKey = "image/unique-key.jpg";
            fileJpaRepository.save(
                    File.builder()
                            .fileKey(fileKey)
                            .storedFileName("unique-key.jpg")
                            .filePath("https://example.com/unique-key.jpg")
                            .refType(AttachmentType.STORE)
                            .refId(1L)
                            .displayOrder(0)
                            .build()
            );

            // when
            File file = fileQueryService.getByFileKey(new GetByFileKeyQuery(fileKey));

            // then
            assertThat(file).isNotNull();
            assertThat(file.getFileKey()).isEqualTo(fileKey);
        }
    }
}

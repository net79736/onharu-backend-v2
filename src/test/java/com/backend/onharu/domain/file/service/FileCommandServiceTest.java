package com.backend.onharu.domain.file.service;

import static com.backend.onharu.domain.support.error.ErrorType.FileOperation.FILE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.dto.FileCommand.DeleteFileCommand;
import com.backend.onharu.domain.file.dto.FileCommand.RegisterFileCommand;
import com.backend.onharu.domain.file.dto.FileQuery.GetByIdQuery;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.file.FileJpaRepository;
import com.backend.onharu.domain.common.TestDataHelper;

@SpringBootTest
@DisplayName("FileCommandService 단위 테스트")
@ActiveProfiles("test")
class FileCommandServiceTest {

    @Autowired
    private FileCommandService fileCommandService;

    @Autowired
    private FileQueryService fileQueryService;

    @Autowired


    private TestDataHelper testDataHelper;


    @Autowired
    private FileJpaRepository fileJpaRepository;

    @BeforeEach
    void setUp() {

        testDataHelper.cleanAll();

    }

    @Nested
    @DisplayName("register 테스트")
    class RegisterTest {

        @Test
        @DisplayName("파일 메타데이터 등록 성공")
        void shouldRegisterFile() {
            // given
            RegisterFileCommand command = new RegisterFileCommand(
                    AttachmentType.STORE,
                    1L,
                    "image/uuid-photo.jpg",
                    "https://example.com/bucket/image/uuid-photo.jpg",
                    "uuid-photo.jpg",
                    "jpg",
                    1024L,
                    0
            );

            // when
            File file = fileCommandService.register(command);

            // then
            assertThat(file).isNotNull();
            assertThat(file.getId()).isNotNull();
            assertThat(file.getFileKey()).isEqualTo("image/uuid-photo.jpg");
            assertThat(file.getFilePath()).isEqualTo("https://example.com/bucket/image/uuid-photo.jpg");
            assertThat(file.getStoredFileName()).isEqualTo("uuid-photo.jpg");
            assertThat(file.getFileExtension()).isEqualTo("jpg");
            assertThat(file.getFileSize()).isEqualTo(1024L);
            assertThat(file.getRefType()).isEqualTo(AttachmentType.STORE);
            assertThat(file.getRefId()).isEqualTo(1L);
            assertThat(file.getDisplayOrder()).isZero();

            // DB에 저장되었는지 확인
            File saved = fileQueryService.getById(new GetByIdQuery(file.getId()));
            assertThat(saved.getFileKey()).isEqualTo(command.fileKey());
        }

        @Test
        @DisplayName("displayOrder가 null이면 0으로 저장됨")
        void shouldUseZeroWhenDisplayOrderIsNull() {
            // given
            RegisterFileCommand command = new RegisterFileCommand(
                    AttachmentType.REVIEW,
                    2L,
                    "image/review.jpg",
                    "https://example.com/bucket/image/review.jpg",
                    "review.jpg",
                    "jpg",
                    null,
                    null
            );

            // when
            File file = fileCommandService.register(command);

            // then
            assertThat(file.getDisplayOrder()).isZero();
        }
    }

    @Nested
    @DisplayName("delete 테스트")
    class DeleteTest {

        @Test
        @DisplayName("파일 삭제 성공")
        void shouldDeleteFile() {
            // given
            File saved = fileJpaRepository.save(
                    File.builder()
                            .fileKey("image/to-delete.jpg")
                            .storedFileName("to-delete.jpg")
                            .filePath("https://example.com/bucket/image/to-delete.jpg")
                            .refType(AttachmentType.STORE)
                            .refId(1L)
                            .displayOrder(0)
                            .build()
            );

            // when
            fileCommandService.delete(new DeleteFileCommand(saved.getId()));

            // then
            CoreException ex = Assertions.assertThrows(
                    CoreException.class,
                    () -> fileQueryService.getById(new GetByIdQuery(saved.getId()))
            );
            assertThat(ex.getErrorType()).isEqualTo(FILE_NOT_FOUND);
        }

        @Test
        @DisplayName("삭제 실패 - 존재하지 않는 ID")
        void shouldThrowExceptionWhenFileNotFound() {
            // given
            Long notExistId = 99999L;

            // when
            CoreException ex = Assertions.assertThrows(
                    CoreException.class,
                    () -> fileCommandService.delete(new DeleteFileCommand(notExistId))
            );

            // then
            assertThat(ex.getErrorType()).isEqualTo(FILE_NOT_FOUND);
        }
    }
}

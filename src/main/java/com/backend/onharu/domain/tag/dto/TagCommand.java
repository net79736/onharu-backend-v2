package com.backend.onharu.domain.tag.dto;

import static com.backend.onharu.domain.support.error.ErrorType.Tag.TAG_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Tag.TAG_NAME_MUST_NOT_BE_BLANK;

import com.backend.onharu.domain.support.error.CoreException;

public class TagCommand {
    /**
     * 태그 생성 커맨드
     */
    public record CreateTagCommand(
            String name
    ) {
        public CreateTagCommand {
            if (name == null || name.isBlank()) {
                throw new CoreException(TAG_NAME_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 태그 수정 커맨드
     */
    public record UpdateTagCommand(
            Long id,
            String name
    ) {
        public UpdateTagCommand {
            if (id == null) {
                throw new CoreException(TAG_ID_MUST_NOT_BE_NULL);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(TAG_NAME_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 태그 삭제 커맨드
     */
    public record DeleteTagCommand(
            Long id
    ) {
        public DeleteTagCommand {
            if (id == null) {
                throw new CoreException(TAG_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}

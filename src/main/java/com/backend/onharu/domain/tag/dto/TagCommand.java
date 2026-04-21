package com.backend.onharu.domain.tag.dto;

public class TagCommand {
    /**
     * 태그 생성 커맨드
     */
    public record CreateTagCommand(
            String name
    ) {
    }

    /**
     * 태그 수정 커맨드
     */
    public record UpdateTagCommand(
            Long id,
            String name
    ) {
    }

    /**
     * 태그 삭제 커맨드
     */
    public record DeleteTagCommand(
            Long id
    ) {
    }
}

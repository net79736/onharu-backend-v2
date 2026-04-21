package com.backend.onharu.domain.store.dto;

public class CategoryCommand {
    /**
     * 카테고리 생성 커맨드
     */
    public record CreateCategoryCommand(
            String name
    ) {
    }

    /**
     * 카테고리 수정 커맨드
     */
    public record UpdateCategoryCommand(
            Long id,
            String name
    ) {
    }

    /**
     * 카테고리 삭제 커맨드
     */
    public record DeleteCategoryCommand(
            Long id
    ) {
    }
}

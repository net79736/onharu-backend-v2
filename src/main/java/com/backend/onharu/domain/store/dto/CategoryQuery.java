package com.backend.onharu.domain.store.dto;

public class CategoryQuery {
    /**
     * 카테고리 ID로 단건 조회
     */
    public record GetCategoryByIdQuery(
            Long categoryId
    ) {
    }

    /**
     * 카테고리 이름으로 목록 검색
     */
    public record FindAllByNameQuery(
            String name
    ) {
    }
}

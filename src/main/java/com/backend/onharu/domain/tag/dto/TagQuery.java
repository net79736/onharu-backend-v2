package com.backend.onharu.domain.tag.dto;

import java.util.List;

public interface TagQuery {
    /**
     * 태그 ID로 단건 조회
     */
    public record GetTagByIdQuery(
            Long tagId
    ) {
    }

    /**
     * 태그 이름으로 검색 (필터링)
     */
    public record FindByNameQuery(
            String name
    ) {
    }

    /**
     * 여러 ID에 해당하는 태그 뭉치 조회
     */
    public record FindByIdsQuery(
            List<Long> tagIds
    ) {
    }

    /**
     * 모든 태그 목록 조회
     */
    public record FindAllTagsQuery() {
    }
}

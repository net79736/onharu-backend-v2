package com.backend.onharu.domain.tag.dto;

import java.util.List;

public class TagQuery {
    /**
     * 태그 ID로 단건 조회
     */
    public record GetTagByIdQuery(
            Long tagId
    ) {
        // public GetTagByIdQuery {
        //     if (tagId == null) {
        //         throw new CoreException(TAG_ID_MUST_NOT_BE_NULL);
        //     }
        // }
    }

    /**
     * 태그 이름으로 검색 (필터링)
     */
    public record FindByNameQuery(
            String name
    ) {
        // public FindTagsByNameQuery {
        //     if (name == null || name.isBlank()) {
        //         throw new CoreException(TAG_NAME_MUST_NOT_BE_BLANK);
        //     }
        // }
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

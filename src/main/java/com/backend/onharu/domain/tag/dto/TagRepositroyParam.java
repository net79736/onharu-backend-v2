package com.backend.onharu.domain.tag.dto;

import java.util.List;

public class TagRepositroyParam {
    /**
     * 태그 ID로 단건 조회용 파라미터
     */
    public record GetTagByIdParam(
        Long tagId
    ) {
    }

    /**
     * 여러 ID에 해당하는 태그 목록 조회용 파라미터
     */
    public record FindAllTagsByIdsParam(
        List<Long> tagIds
    ) {
    }

    /**
     * 태그 이름으로 태그 목록 조회용 파라미터
     */
    public record FindAllByNameParam(
        String name
    ) {
    }
}

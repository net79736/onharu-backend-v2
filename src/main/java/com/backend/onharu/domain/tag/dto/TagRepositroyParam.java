package com.backend.onharu.domain.tag.dto;

import java.util.List;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

public class TagRepositroyParam {
    /**
     * 태그 ID로 단건 조회용 파라미터
     */
    public record GetTagByIdParam(
        Long tagId
    ) {
        public GetTagByIdParam {
            if (tagId == null) {
                throw new CoreException(ErrorType.Tag.TAG_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 여러 ID에 해당하는 태그 목록 조회용 파라미터
     */
    public record FindByIdsParam(
        List<Long> tagIds
    ) {
        public FindByIdsParam {
            if (tagIds == null || tagIds.isEmpty()) {
                throw new CoreException(ErrorType.Tag.TAG_IDS_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 태그 이름으로 태그 목록 조회용 파라미터
     */
    public record FindByNameParam(
        String name
    ) {
    }
}

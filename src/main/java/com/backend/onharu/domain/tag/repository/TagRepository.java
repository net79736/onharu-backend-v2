package com.backend.onharu.domain.tag.repository;

import java.util.List;

import com.backend.onharu.domain.tag.dto.TagRepositroyParam.FindAllByNameParam;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.FindAllTagsByIdsParam;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.GetTagByIdParam;
import com.backend.onharu.domain.tag.model.Tag;

public interface TagRepository {
    /**
     * 태그 저장 및 수정
     */
    Tag save(Tag tag);

    /**
     * 태그 단건 조회
     */
    Tag getTag(GetTagByIdParam param);

    /**
     * ID 리스트로 여러 태그 조회
     */
    List<Tag> findAllByIds(FindAllTagsByIdsParam param);

    /**
     * 태그 이름으로 여러건 조회
     */
    List<Tag> findAllByName(FindAllByNameParam param);

    /**
     * 태그 삭제
     */
    void delete(Tag tag);
}

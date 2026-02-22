package com.backend.onharu.infra.nts;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 국세청 사업자등록정보 상태조회 서비스에 사용될 DTO 입니다.
 */
public record NtsResponse(
        List<Data> data
) {
    public record Data(
            @JsonProperty("b_stt")
            String businessStatus
    ) {
    }
}

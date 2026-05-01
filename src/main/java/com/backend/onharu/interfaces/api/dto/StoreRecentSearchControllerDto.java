package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StoreRecentSearchControllerDto {
    @Schema(description = "가게 검색 최근 검색어 저장 요청")
    public record RecentKeywordRequest(
            @NotBlank
            @Size(max = 100)
            @Schema(description = "검색 키워드", example = "베이커리")
            String keyword
    ) {
    }
}

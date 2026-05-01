package com.backend.onharu.interfaces.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.StoreRecentSearchControllerDto.RecentKeywordRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "Store recent search", description = "가게 검색 최근 검색어 (Redis)")
public interface IStoreRecentSearchController {

    @Operation(
            summary = "가게 검색 최근 검색어 목록",
            description = "로그인 시 사용자별, 비로그인 시 쿠키(clientId) 기반으로 Redis 최근 검색어를 반환합니다. 쿠키가 없으면 서버에서 UUID를 발급해 쿠키로 내려줍니다."
    )
    ResponseEntity<ResponseDTO<List<String>>> listRecentKeywords(
            HttpServletRequest request,
            HttpServletResponse response
    );

    @Operation(
            summary = "가게 검색 최근 검색어 저장",
            description = "검색 실행 시 최근 검색어를 Redis에 기록합니다."
    )
    ResponseEntity<ResponseDTO<Void>> recordRecentKeyword(
            @Valid @RequestBody RecentKeywordRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse response
    );
}

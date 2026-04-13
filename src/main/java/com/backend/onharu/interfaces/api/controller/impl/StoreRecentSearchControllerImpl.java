package com.backend.onharu.interfaces.api.controller.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.application.StoreRecentSearchService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IStoreRecentSearchController;
import com.backend.onharu.interfaces.api.dto.StoreRecentSearchControllerDto.RecentKeywordRequest;
import com.backend.onharu.interfaces.api.support.ClientIdentityResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 가게 검색 최근 검색어 API. Redis 미구성 시 {@link StoreRecentSearchService} 빈이 없으면 목록은 빈 값, 저장은 성공 응답(null) 처리.
 */
@Slf4j
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreRecentSearchControllerImpl implements IStoreRecentSearchController {
    private final StoreRecentSearchService recentSearchService;
    private final ClientIdentityResolver clientIdentityResolver;

    @Override
    @GetMapping("/recent-keywords")
    public ResponseEntity<ResponseDTO<List<String>>> listRecentKeywords(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String owner = clientIdentityResolver.resolveOwnerKey(request, response);
        return ResponseEntity.ok(ResponseDTO.success(recentSearchService.list(owner)));
    }

    @Override
    @PostMapping("/recent-keywords")
    public ResponseEntity<ResponseDTO<Void>> recordRecentKeyword(
            @Valid @RequestBody RecentKeywordRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse response
    ) {
        String owner = clientIdentityResolver.resolveOwnerKey(servletRequest, response);
        if (owner == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        recentSearchService.record(owner, request.keyword());
        return ResponseEntity.ok(ResponseDTO.success(null));
    }
}

package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.ChildFacade;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoritesByChildIdQuery;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;
import com.backend.onharu.interfaces.api.controller.IFavoriteController;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.FavoriteResponse;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.FavoriteToggleResponse;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.GetMyFavoriteListResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto;
import com.backend.onharu.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.backend.onharu.domain.favorite.dto.FavoriteCommand.ToggleFavoriteCommand;

@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteControllerImpl implements IFavoriteController {

    private final ChildFacade childFacade;

    /**
     * 찜등록/찜취소 (토글)
     *
     * POST /api/favorites/stores/{storeId}
     * 특정 가게에 대한 찜을 등록/취소 합니다.
     *
     * @param storeId 가게 ID
     */
    @Override
    @PostMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<FavoriteToggleResponse>> createOrDeleteFavorite(
            @PathVariable Long storeId
    ) {
        // 현재 세션에 저장된 아동 ID 추출
        Long childId = SecurityUtils.getCurrentUserId();

        log.info("찜하기 요청(토글): childId={}, storeId={}", childId, storeId);

        // 찜등록/찜취소 실행
        boolean isFavorite = childFacade.toggleFavorite(
                new ToggleFavoriteCommand(childId, storeId)
        );

        // 응답 생성
        FavoriteToggleResponse response = new FavoriteToggleResponse(isFavorite);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

    /**
     * 내 찜목록 조회
     *
     * GET /favorites
     * 내가 작성한 찜목록을 조회합니다.
     *
     * @return 내가 작성한 찜목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ResponseDTO<GetMyFavoriteListResponse>> getMyFavorite(
            @ParameterObject
            @ModelAttribute ReviewControllerDto.GetReviewsRequest request
    ) {
        Long childId = SecurityUtils.getCurrentUserId();

        log.info("찜목록 조회 요청: childId={}", childId);

        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(),
                request.perPage(),
                request.sortField(),
                request.sortDirection()
        ); // 페이징 정보

        // 내 찜목록 조회
        Page<Favorite> favorites = childFacade.getMyFavorites(
                new FindFavoritesByChildIdQuery(childId),
                pageable
        );

        // 응답 리스트 생성
        Page<FavoriteResponse> favoriteResponses = favorites.map(FavoriteResponse::new);

        GetMyFavoriteListResponse response = new GetMyFavoriteListResponse(
                favoriteResponses.getContent(),
                favoriteResponses.getTotalElements(),
                favoriteResponses.getNumber() + 1,
                favoriteResponses.getTotalPages(),
                favoriteResponses.getSize()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

}

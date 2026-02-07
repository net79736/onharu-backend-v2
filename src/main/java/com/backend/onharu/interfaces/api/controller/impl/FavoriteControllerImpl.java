package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.ChildFacade;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.CreateFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.DeleteFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoritesByChildIdQuery;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IFavoriteController;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.FavoriteResponse;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.CreateFavoriteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.GetMyFavoriteListResponse;

@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteControllerImpl implements IFavoriteController {

    private final ChildFacade childFacade;

    /**
     * 찜등록
     *
     * POST /favorites/stores/{storeId}
     * 특정 가게에 대한 찜을 등록합니다.
     *
     * @param storeId 찜하기 ID
     * @return 생성된 찜하기 정보
     */
    @Override
    @PostMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<CreateFavoriteResponse>> createFavorite(@PathVariable("storeId") Long storeId) {
        log.info("찜하기 등록 요청: storeId={}", storeId);

        // TODO: 하드코딩 - 시큐리티 세션에서 현재 로그인한 사용자(아동) ID 가져오기
        Long childId = 1L;

        // 찜등록
        Favorite favorite = childFacade.createFavorite(
                new CreateFavoriteCommand(childId, storeId)
        );

        // 응답 생성
        CreateFavoriteResponse response = new CreateFavoriteResponse(favorite.getId());

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
    public ResponseEntity<ResponseDTO<GetMyFavoriteListResponse>> getMyFavorite() {
        log.info("찜목록 조회 요청");

        // TODO: 하드코딩 - 시큐리티 세션에서 현재 로그인한 사용자(아동) ID 가져오기
        Long childId = 1L;

        // 내 찜목록 조회
        List<Favorite> favorites = childFacade.getMyFavorites(
                new FindFavoritesByChildIdQuery(childId)
        );

        // 응답 리스트 생성
        List<FavoriteResponse> favoriteResponses = favorites.stream()
                .map(FavoriteResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(new GetMyFavoriteListResponse(favoriteResponses)));
    }

    /**
     * 내가 등록한 찜하기 취소
     *
     * DELETE /favorites/{favoriteId}
     * 특정 찜하기를 삭제(취소)합니다.
     *
     * @param favoriteId 찜하기 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ResponseDTO<Void>> deleteFavorite(@PathVariable("favoriteId") Long favoriteId) {
        log.info("찜취소 요청: favoriteId={}", favoriteId);

        // TODO: 하드코딩 - 시큐리티 세션에서 현재 로그인한 사용자(아동) ID 가져오기
        Long childId = 1L;

        // 찜하기 취소
        childFacade.deleteFavorite(
                new DeleteFavoriteCommand(childId, favoriteId)
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}

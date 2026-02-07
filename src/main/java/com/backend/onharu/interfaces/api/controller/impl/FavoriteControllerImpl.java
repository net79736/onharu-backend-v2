package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IFavoriteController;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.CreateFavoriteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.GetMyFavoriteListResponse;

@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteControllerImpl implements IFavoriteController {

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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}

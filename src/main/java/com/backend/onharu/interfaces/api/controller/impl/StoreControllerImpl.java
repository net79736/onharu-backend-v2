package com.backend.onharu.interfaces.api.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IStoreController;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreListResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UpdateStoreRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 가게 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 가게의 상세 정보 조회, 목록 조회, 생성, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreControllerImpl implements IStoreController {

    /**
     * 가게 상세 정보 조회
     * 
     * GET /stores/{storeId}
     * 특정 가게의 상세 정보를 반환합니다.
     *
     * @param storeId 가게 ID
     * @return 가게 상세 정보
     */
    @Override
    @GetMapping("/{storeId}")
    public ResponseEntity<ResponseDTO<GetStoreDetailResponse>> getStore(
            @PathVariable Long storeId
    ) {
        log.info("가게 상세 정보 조회 요청: storeId={}", storeId);
        

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 목록 조회
     * 
     * GET /stores
     * 검색 및 페이징이 가능한 가게 목록을 반환합니다.
     *
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 반경(km)
     * @return 가게 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ResponseDTO<GetStoreListResponse>> searchStores(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radius
    ) {
        log.info("가게 목록 조회 요청: latitude={}, longitude={}, radius={}", latitude, longitude, radius);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 정보 작성
     * 
     * POST /stores
     * 신규 가게 정보를 생성합니다.
     *
     * @param request 가게 정보 생성 요청
     * @return 생성된 가게 정보
     */
    @Override
    @PostMapping
    public ResponseEntity<ResponseDTO<OpenStoreResponse>> openStore(
            @RequestBody OpenStoreRequest request
    ) {
        log.info("가게 정보 작성 요청: {}", request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 정보 삭제
     * 
     * DELETE /stores/{storeId}
     * 특정 가게 정보를 삭제합니다.
     *
     * @param storeId 가게 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ResponseDTO<Void>> closeStore(
            @PathVariable Long storeId
    ) {
        log.info("가게 정보 삭제 요청: storeId={}", storeId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 정보 수정
     * 
     * PUT /stores/{storeId}
     * 특정 가게 정보를 수정합니다.
     *
     * @param storeId 가게 ID
     * @param request 가게 정보 수정 요청
     * @return 수정 결과
     */
    @Override
    @PutMapping("/{storeId}")
    public ResponseEntity<ResponseDTO<Void>> updateMyStore(
            @PathVariable Long storeId,
            @RequestBody UpdateStoreRequest request
    ) {
        log.info("가게 정보 수정 요청: storeId={}, request={}", storeId, request);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}

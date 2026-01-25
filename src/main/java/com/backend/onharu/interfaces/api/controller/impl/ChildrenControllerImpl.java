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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IChildrenController;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetCardResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetCertificateResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetLikedStoreListResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.IssueCardRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.IssueCardResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.UpdateCardRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.UpdateCertificateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결식 아동 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 결식 아동 카드, 증명서 관리 및 관심 가게 목록 조회 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/children")
@RequiredArgsConstructor
public class ChildrenControllerImpl implements IChildrenController {

    /**
     * 결식 아동 카드 등록
     * 
     * POST /children/cards
     * 새로운 결식 아동 카드를 등록합니다.
     *
     * @param request 카드 등록 요청
     * @return 결식 아동 카드 등록 결과
     */
    @Override
    @PostMapping("/cards")
    public ResponseEntity<ResponseDTO<IssueCardResponse>> issueCard(
            @RequestBody IssueCardRequest request
    ) {
        log.info("결식 아동 카드 등록 요청: {}", request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 수정
     * 
     * PUT /children/cards/{cardId}
     * 특정 카드 정보를 수정합니다.
     *
     * @param cardId 카드 ID
     * @param request 카드 수정 요청
     * @return 결식 아동 카드 수정 결과
     */
    @Override
    @PutMapping("/cards/{cardId}")
    public ResponseEntity<ResponseDTO<Void>> updateCard(
            @PathVariable Long cardId,
            @RequestBody UpdateCardRequest request
    ) {
        log.info("결식 아동 카드 수정 요청: cardId={}, request={}", cardId, request);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 삭제
     * 
     * DELETE /children/cards/{cardId}
     * 특정 카드를 삭제합니다.
     *
     * @param cardId 카드 ID
     * @return
     */
    @Override
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<ResponseDTO<Void>> deleteCard(
            @PathVariable Long cardId
    ) {
        log.info("결식 아동 카드 삭제 요청: cardId={}", cardId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 재발급 요청
     * 
     * POST /children/cards/{cardId}/reissue
     * 카드 재발급을 요청합니다.
     *
     * @param cardId 카드 ID
     * @return 재발급 요청 결과
     */
    @Override
    @PostMapping("/cards/{cardId}/reissue")
    public ResponseEntity<ResponseDTO<Void>> reissueCard(
            @PathVariable Long cardId
    ) {
        log.info("결식 아동 카드 재발급 요청: cardId={}", cardId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 조회
     * 
     * GET /children/cards/{cardId}
     * 특정 카드 정보를 조회합니다.
     *
     * @param cardId 카드 ID
     * @return 카드 정보
     */
    @Override
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<ResponseDTO<GetCardResponse>> getMyCard(
            @PathVariable Long cardId
    ) {
        log.info("결식 아동 카드 조회 요청: cardId={}", cardId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 등록
     * 
     * POST /children/certificate
     * 증명서를 등록합니다. (첨부파일 포함)
     *
     * @param request 증명서 등록 요청
     * @param file 증명서 파일
     * @return 등록 결과
     */
    @Override
    @PostMapping("/certificate")
    public ResponseEntity<ResponseDTO<Void>> uploadCertificate(
            @RequestPart UpdateCertificateRequest request,
            @RequestPart MultipartFile file
    ) {
        log.info("결식 아동 증명서 등록 요청: request={}, fileName={}", request, file.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 수정
     * 
     * PUT /children/certificate/{certificateId}
     * 증명서를 수정합니다. (첨부파일 포함)
     *
     * @param certificateId 증명서 ID
     * @param request 증명서 수정 요청
     * @param file 증명서 파일
     * @return 수정 결과
     */
    @Override
    @PutMapping("/certificate/{certificateId}")
    public ResponseEntity<ResponseDTO<Void>> updateMyCertificate(
            @PathVariable Long certificateId,
            @RequestPart UpdateCertificateRequest request,
            @RequestPart MultipartFile file
    ) {
        log.info("결식 아동 증명서 수정 요청: certificateId={}, request={}, fileName={}", 
                certificateId, request, file.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 삭제
     * 
     * DELETE /children/certificate/{certificateId}
     * 증명서를 삭제합니다. (첨부파일 포함)
     *
     * @param certificateId 증명서 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/certificate/{certificateId}")
    public ResponseEntity<ResponseDTO<Void>> removeMyCertificate(
            @PathVariable Long certificateId
    ) {
        log.info("결식 아동 증명서 삭제 요청: certificateId={}", certificateId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 조회
     * 
     * GET /children/certificate/{certificateId}
     * 증명서를 조회합니다. (첨부파일 포함)
     *
     * @param certificateId 증명서 ID
     * @return 증명서 정보
     */
    @Override
    @GetMapping("/certificate/{certificateId}")
    public ResponseEntity<ResponseDTO<GetCertificateResponse>> getMyCertificate(
            @PathVariable Long certificateId
    ) {
        log.info("결식 아동 증명서 조회 요청: certificateId={}", certificateId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 관심 가게 목록 조회
     * 
     * GET /children/favorite-stores
     * 관심 매장 목록을 페이지 형태로 조회합니다.
     *
     * @return 관심 가게 목록
     */
    @Override
    @GetMapping("/favorite-stores")
    public ResponseEntity<ResponseDTO<GetLikedStoreListResponse>> getFavoriteStores() {
        log.info("관심 가게 목록 조회 요청");
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}

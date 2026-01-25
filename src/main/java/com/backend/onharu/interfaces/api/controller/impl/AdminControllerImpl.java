package com.backend.onharu.interfaces.api.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IAdminController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 사업자 및 결식 아동 회원가입 요청 승인/거절 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminControllerImpl implements IAdminController {

    /**
     * 사업자 회원가입 요청 승인
     * 
     * POST /admin/owners/{requestId}/approve
     * 사업자 회원가입 요청을 승인합니다.
     *
     * @param requestId 요청 ID
     * @return 승인 결과
     */
    @Override
    @PostMapping("/owners/{requestId}/approve")
    public ResponseEntity<ResponseDTO<Void>> approveOwnerSignup(
            @PathVariable Long requestId
    ) {
        log.info("사업자 회원가입 요청 승인: requestId={}", requestId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 회원가입 요청 거절
     * 
     * POST /admin/owners/{requestId}/reject
     * 사업자 회원가입 요청을 거절합니다.
     *
     * @param requestId 요청 ID
     * @return 거절 결과
     */
    @Override
    @PostMapping("/owners/{requestId}/reject")
    public ResponseEntity<ResponseDTO<Void>> rejectOwnerSignup(
            @PathVariable Long requestId
    ) {
        log.info("사업자 회원가입 요청 거절: requestId={}", requestId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 회원가입 요청 승인
     * 
     * POST /admin/children/{requestId}/approve
     * 결식 아동 회원가입 요청을 승인합니다.
     *
     * @param requestId 요청 ID
     * @return 승인 결과
     */
    @Override
    @PostMapping("/children/{requestId}/approve")
    public ResponseEntity<ResponseDTO<Void>> approveChildSignup(
            @PathVariable Long requestId
    ) {
        log.info("결식 아동 회원가입 요청 승인: requestId={}", requestId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 회원가입 요청 거절
     * 
     * POST /admin/children/{requestId}/reject
     * 결식 아동 회원가입 요청을 거절합니다.
     *
     * @param requestId 요청 ID
     * @return 거절 결과
     */
    @Override
    @PostMapping("/children/{requestId}/reject")
    public ResponseEntity<ResponseDTO<Void>> rejectChildSignup(
            @PathVariable Long requestId
    ) {
        log.info("결식 아동 회원가입 요청 거절: requestId={}", requestId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}

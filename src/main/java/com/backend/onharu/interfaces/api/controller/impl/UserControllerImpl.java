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
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IUserController;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.CreateUserRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.CreateUserResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.GetUserResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateUserRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 사용자 정보 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {

    /**
     * 사용자 회원가입
     * 
     * POST /users
     * 사용자 회원가입을 진행 합니다.
     *
     * @param request 사용자 회원가입 요청
     * @return 사용자 회원가입 결과
     */
    @Override
    @PostMapping
    public ResponseEntity<ResponseDTO<CreateUserResponse>> createUser(
            @RequestBody CreateUserRequest request
    ) {
        log.info("사용자 정보 등록 요청: {}", request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사용자 정보 조회
     * 
     * GET /users/{userId}
     * 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseDTO<GetUserResponse>> getUser(
            @PathVariable Long userId
    ) {
        log.info("사용자 정보 조회 요청: userId={}", userId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사용자 정보 수정
     * 
     * PUT /users/{userId}
     * 사용자 정보를 수정합니다.
     *
     * @param userId 사용자 ID
     * @param request 사용자 정보 수정 요청
     * @return 수정 결과
     */
    @Override
    @PutMapping("/{userId}")
    public ResponseEntity<ResponseDTO<Void>> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request
    ) {
        log.info("사용자 정보 수정 요청: userId={}, request={}", userId, request);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사용자 회원 탈퇴
     * 
     * DELETE /users/{userId}
     * 사용자 정보를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(
            @PathVariable Long userId
    ) {
        log.info("사용자 정보 삭제 요청: userId={}", userId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}

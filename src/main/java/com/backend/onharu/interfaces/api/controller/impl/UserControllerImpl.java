package com.backend.onharu.interfaces.api.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IUserController;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpChildRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpChildResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpOwnerRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpOwnerResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateChildProfileRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateOwnerProfileRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 역할별 회원가입, 프로필 조회/수정, 사용자 정보 관리 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {

    /**
     * 아동 회원가입
     * 
     * POST /users/signup/child
     * 아동 회원가입을 진행합니다. 사용자 정보와 증명서 파일을 함께 받습니다.
     *
     * @param request 아동 회원가입 요청
     * @param certificateFile 증명서 파일
     * @return 회원가입 결과
     */
    @Override
    @PostMapping("/signup/child")
    public ResponseEntity<ResponseDTO<SignUpChildResponse>> signUpChild(
            @RequestPart SignUpChildRequest request,
            @RequestPart MultipartFile certificateFile
    ) {
        log.info("아동 회원가입 요청: request={}, fileName={}", request, certificateFile.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 회원가입
     * 
     * POST /users/signup/owner
     * 사업자 회원가입을 진행합니다. 사용자 정보, 사업자 정보, 사업자 등록 서류 파일을 함께 받습니다.
     *
     * @param request 사업자 회원가입 요청
     * @param businessRegistrationFile 사업자 등록 서류 파일
     * @return 회원가입 결과
     */
    @Override
    @PostMapping("/signup/owner")
    public ResponseEntity<ResponseDTO<SignUpOwnerResponse>> signUpOwner(
            @RequestPart SignUpOwnerRequest request,
            @RequestPart MultipartFile businessRegistrationFile
    ) {
        log.info("사업자 회원가입 요청: request={}, fileName={}", request, businessRegistrationFile.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사용자 프로필 조회
     * 
     * GET /users/{userId}/profile
     * Spring Security Context에서 현재 사용자의 역할을 확인하여 역할별 프로필을 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 역할별 프로필 정보
     */
    @Override
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ResponseDTO<?>> getProfile(
            @PathVariable Long userId
    ) {
        log.info("사용자 프로필 조회 요청: userId={}", userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사용자 프로필 수정
     * 
     * PUT /users/{userId}/profile
     * Spring Security Context에서 현재 사용자의 역할을 확인하여 역할별 프로필을 수정합니다.
     *
     * @param userId 사용자 ID
     * @param childRequest 아동 프로필 수정 요청 (아동인 경우)
     * @param ownerRequest 사업자 프로필 수정 요청 (사업자인 경우)
     * @param certificateFile 증명서 파일 (아동인 경우)
     * @param businessRegistrationFile 사업자 등록 서류 파일 (사업자인 경우)
     * @return 수정 결과
     */
    @Override
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ResponseDTO<Void>> updateProfile(
            @PathVariable Long userId,
            @RequestPart(required = false) UpdateChildProfileRequest childRequest,
            @RequestPart(required = false) UpdateOwnerProfileRequest ownerRequest,
            @RequestPart(required = false) MultipartFile certificateFile,
            @RequestPart(required = false) MultipartFile businessRegistrationFile
    ) {
        log.info("사용자 프로필 수정 요청: userId={}", userId);
        
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

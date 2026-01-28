package com.backend.onharu.interfaces.api.controller.impl;

import jakarta.validation.Valid;
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

import com.backend.onharu.application.UserFacade;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.model.User;
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

    private final UserFacade userFacade;

    /**
     * 사업자 회원가입
     *
     * POST /users/signup/owner
     * 사업자 회원가입을 진행합니다. 사용자 정보와 사업자 정보를 함께 받습니다.
     *
     * @param request 사업자 회원가입 요청
     * @return 회원가입 결과
     */
    @Override
    @PostMapping("/signup/owner")
    public ResponseEntity<ResponseDTO<SignUpOwnerResponse>> signUpOwner(
            @Valid @RequestBody SignUpOwnerRequest request
    ) {
        log.info("사업자 회원가입 요청: request={}", request);

        // Command 생성
        SignUpOwnerCommand command = new SignUpOwnerCommand(
                request.loginId(),
                request.password(),
                request.passwordConfirm(),
                request.name(),
                request.phone(),
                request.storeName(),
                request.businessNumber(),
                request.levelId()
        );

        // 회원가입 처리
        User user = userFacade.signUpOwner(command);

        // 응답 생성
        SignUpOwnerResponse response = new SignUpOwnerResponse(
                user.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

    /**
     * 아동 회원가입
     *
     * POST /users/signup/child
     * 아동 회원가입을 진행합니다. 사용자 정보와 증명서 파일 URL을 함께 받습니다.
     *
     * @param request 아동 회원가입 요청 (증명서 파일 URL 포함)
     * @return 회원가입 결과
     */
    @Override
    @PostMapping("/signup/child")
    public ResponseEntity<ResponseDTO<SignUpChildResponse>> signUpChild(
            @Valid @RequestBody SignUpChildRequest request
    ) {
        log.info("아동 회원가입 요청: request={}", request);

        // Command 생성
        SignUpChildCommand command = new SignUpChildCommand(
                request.loginId(),
                request.password(),
                request.passwordConfirm(),
                request.name(),
                request.phone(),
                request.nickname(),
                request.certificate()
        );

        // 회원가입 처리
        User user = userFacade.signUpChild(command);

        // 응답 생성
        SignUpChildResponse response = new SignUpChildResponse(
                user.getId(),
                user.getLoginId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
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
     * 사용자 프로필을 수정합니다.
     * 
     * @param userId 사용자 ID
     * @param childRequest 아동 프로필 수정 요청
     * @param ownerRequest 사업자 프로필 수정 요청
     * @return
     */
    @Override
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ResponseDTO<Void>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateChildProfileRequest childRequest,
            @Valid @RequestBody UpdateOwnerProfileRequest ownerRequest
    ) {
        log.info("사용자 프로필 수정 요청: userId={}, childRequest={}, ownerRequest={}", userId, childRequest, ownerRequest);
        
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

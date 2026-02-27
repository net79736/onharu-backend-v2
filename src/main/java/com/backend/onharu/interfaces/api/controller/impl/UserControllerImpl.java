package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.UserFacade;
import com.backend.onharu.application.dto.UserLogin;
import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.file.dto.FileCommand;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.service.FileQueryService;
import com.backend.onharu.domain.user.dto.UserCommand.*;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpChildUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpOwnerUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserProfile.UserChildProfile;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.security.LocalUser;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IUserController;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.*;
import com.backend.onharu.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.backend.onharu.domain.file.dto.FileQuery.ListByRefQuery;
import static com.backend.onharu.domain.user.dto.UserProfile.UserOwnerProfile;
import static com.backend.onharu.domain.user.dto.UserQuery.*;
import static com.backend.onharu.interfaces.api.common.dto.ImageMetadataRequest.toImageMetadataList;

/**
 * 사용자 관련 API를 제공하는 컨트롤러 구현체입니다.
 * <p>
 * 역할별 회원가입, 프로필 조회/수정, 사용자 정보 관리 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {

    private final UserFacade userFacade;

    private final FileQueryService fileQueryService;

    /**
     * 사업자 회원가입
     * <p>
     * POST /api/users/signup/owner
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

        // 사업자 회원가입 Command
        SignUpOwnerCommand command = new SignUpOwnerCommand(
                request.loginId(),
                request.password(),
                request.passwordConfirm(),
                request.name(),
                request.phone(),
                request.businessNumber()
        );

        // 사업자 회원가입
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
     * <p>
     * POST /api/users/signup/child
     * 아동 회원가입을 진행합니다. 사용자 정보와 증명서 파일 URL 을 함께 받습니다.
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

        // 아동 회원가입 Command
        SignUpChildCommand command = new SignUpChildCommand(
                request.loginId(),
                request.password(),
                request.passwordConfirm(),
                request.name(),
                request.phone(),
                request.nickname(),
                toImageMetadataList(request.images()) // 요청의 이미지 메타데이터 목록을 도메인 ImageMetadata 목록으로 변환후 Command 에 넣음
        );

        // 아동 회원가입
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
     * 사용자(아동) 프로필 조회
     * <p>
     * GET /api/users/profile/child
     *
     * @return 아동 프로필 정보
     */
    @Override
    @GetMapping("/profile/child")
    public ResponseEntity<ResponseDTO<ChildProfileResponse>> getChildProfile() {
        log.info("아동 프로필 조회");

        Long userId = SecurityUtils.getUserId(); // 세션에 인증된 사용자 ID 추출
        Long childId = SecurityUtils.getCurrentUserId();// 세션에 인증된 아동 ID 추출

        UserChildProfile childProfile = userFacade.getChildProfile(new GetChildProfileQuery(userId, childId)); // 프로필 조회

        // 증명서류 조회
        List<File> files = fileQueryService.listByRef(
                new ListByRefQuery(AttachmentType.CHILD, childProfile.child().getId())
        );

        // 증명서류 파일 경로 추출
        List<String> imagePaths = files.stream()
                .map(File::getFilePath)
                .toList();

        // 응답 생성
        ChildProfileResponse response = new ChildProfileResponse(
                childProfile.user().getLoginId(),
                childProfile.user().getName(),
                childProfile.user().getPhone(),
                childProfile.child().getNickname(),
                imagePaths
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 사용자(사업자) 프로필 조회
     * <p>
     * GET /api/users/profile/owner
     *
     * @return 사업자 프로필 정보
     */
    @Override
    @GetMapping("/profile/owner")
    public ResponseEntity<ResponseDTO<OwnerProfileResponse>> getOwnerProfile() {
        log.info("사업자 프로필 조회");

        Long userId = SecurityUtils.getUserId(); // 세션에 인증된 사용자 ID 추출
        Long ownerId = SecurityUtils.getCurrentUserId();// 세션에 인증된 사업자 ID 추출

        UserOwnerProfile ownerProfile = userFacade.getOwnerProfile(new GetOwnerProfileQuery(userId, ownerId)); // 프로필 조회

        // 응답 생성
        OwnerProfileResponse response = new OwnerProfileResponse(
                ownerProfile.user().getLoginId(),
                ownerProfile.user().getName(),
                ownerProfile.user().getPhone(),
                ownerProfile.level().getName(),
                ownerProfile.owner().getBusinessNumber()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 사용자(아동) 프로필 수정
     * <p>
     * PUT /api/users/profile/child
     *
     * @param childRequest 수정할 프로필 정보(이름, 전화번호, 닉네임)
     */
    @Override
    @PutMapping("/profile/child")
    public ResponseEntity<ResponseDTO<String>> updateChildProfile(
            @Valid @RequestBody UpdateChildProfileRequest childRequest
    ) {
        log.info("아동 프로필 수정");

        Long userId = SecurityUtils.getUserId(); // 세션에 인증된 사용자 ID 추출
        Long childId = SecurityUtils.getCurrentUserId();// 세션에 인증된 아동 ID 추출

        // 프로필 수정(사용자+아동)
        userFacade.updateChildProfile(
                new UpdateChildProfileCommand(
                        userId,
                        childId,
                        childRequest.name(),
                        childRequest.phone(),
                        childRequest.nickname()
                )
        );

        String response = "아동 프로필 수정 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 사용자(사업자) 프로필 수정
     * <p>
     * PUT /api/users/profile/owner
     *
     * @param ownerRequest 수정할 프로필 정보(이름, 전화번호, 등급 ID, 사업자 번호)
     */
    @Override
    @PutMapping("/profile/owner")
    public ResponseEntity<ResponseDTO<String>> updateOwnerProfile(
            @Valid @RequestBody UpdateOwnerProfileRequest ownerRequest
    ) {
        log.info("사업자 프로필 수정 request: {}", ownerRequest);

        Long userId = SecurityUtils.getUserId(); // 세션에 인증된 사용자 ID 추출
        Long ownerId = SecurityUtils.getCurrentUserId();// 세션에 인증된 사업자 ID 추출

        // 프로필 수정(사용자+사업자)
        userFacade.updateOwnerProfile(
                new UpdateOwnerProfileCommand(
                        userId,
                        ownerId,
                        ownerRequest.name(),
                        ownerRequest.phone(),
                        ownerRequest.businessNumber()
                )
        );

        String response = "사업자 프로필 수정 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 사용자 회원 탈퇴
     * <p>
     * DELETE /api/users
     * 사용자 계정상태를 삭제됨으로 변경합니다.
     */
    @Override
    @DeleteMapping
    public ResponseEntity<ResponseDTO<String>> deleteUser() {
        log.info("사용자 정보 삭제 요청");

        Long userId = SecurityUtils.getUserId(); // 세션에 인증된 사용자 ID 추출

        // 회원 탈퇴 갱신
        userFacade.updateDeletedUser(
                new UpdateDeletedUserCommand(
                        userId,
                        StatusType.DELETED
                )
        );

        String response = "회원 탈퇴 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 로컬 사용자 로그인
     * <p>
     * POST /api/users/login
     * 사용자 아이디와 비밀번호로 로그인을 수행합니다.
     *
     * @param request 사용자 아이디, 비밀번호
     * @return 200 OK
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<String>> login(
            @Valid @RequestBody LoginUserRequest request, HttpServletRequest httpRequest
    ) {
        log.info("사용자 로그인 요청: loginUserRequest={}", request);

        // 사용자 로그인
        UserLogin userLogin = userFacade.loginUser(
                new LoginUserCommand(
                        request.loginId(),
                        request.password()
                )
        );

        LocalUser localUser = new LocalUser(userLogin.user(), userLogin.domainId()); // 로그인 사용자 정보를 UserDetails 구현체 변환
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(localUser, null, localUser.getAuthorities()); // 인증 객체 생성

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(usernamePasswordAuthenticationToken); // SecurityContext 에 인증 정보 저장

        httpRequest.getSession(true)
                .setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        securityContext
                ); // 세션 생성

        String response = "로그인 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 로컬 사용자 로그아웃
     * <p>
     * POST /api/users/logout
     * 로그인된 사용자의 로그아웃을 수행합니다.
     *
     * @return 200 OK
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<String>> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("사용자 로그아웃 요청");

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication(); // 인증 정보(= 사용자 로그인 상태)

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication); // 세션 무효화 및 인증 삭제
        }

        String response = "로그아웃 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 소셜 사용자(아동) 회원가입 마무리
     * <p>
     * POST /api/users/signup/child/finish
     */
    @Override
    @PostMapping("/signup/child/finish")
    public ResponseEntity<ResponseDTO<SignUpChildResponse>> finishSignUpChild(
            @Valid @RequestBody finishSignUpChildRequest request) {
        log.info("소셜 사용자 아동 회원가입 마무리 요청: {}", request);

        // 소셜 로그인에 인증된 사용자 ID 추출
        String userId = Objects.requireNonNull(SecurityUtils.getCurrentOAuth2User()).getName();

        // 요청의 이미지 메타데이터 목록을 도메인 ImageMetadata 목록으로 변환
        List<FileCommand.ImageMetadata> images = toImageMetadataList(request.images());

        // 소셜 사용자 아동 회원가입 Command 생성
        SignUpChildUserOAuthCommand command = new SignUpChildUserOAuthCommand(
                userId,
                request.nickname(),
                images
        );

        // 소셜 사용자 아동 회원가입
        UserLogin userLogin = userFacade.completeSignUpChildUserOAuth(command);

        // 소셜 사용자 인증 업데이트
        SecurityUtils.renewSocialUserAuthentication(userLogin.user(), userLogin.domainId());

        // 응답 생성
        SignUpChildResponse response = new SignUpChildResponse(
                userLogin.user().getId(),
                userLogin.user().getLoginId()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 소셜 사용자(사업자) 회원가입 마무리
     * <p>
     * POST /api/users/signup/owner/finish
     */
    @Override
    @PostMapping("/signup/owner/finish")
    public ResponseEntity<ResponseDTO<SignUpChildResponse>> finishSignUpOwner(
            @Valid @RequestBody finishSignUpOwnerRequest request) {
        log.info("소셜 사용자 사업자 회원가입 마무리 요청");

        // 소셜 로그인에 인증된 사용자 ID 추출
        String userId = Objects.requireNonNull(SecurityUtils.getCurrentOAuth2User()).getName();

        // 소셜 사용자(사업자) 회원가입
        UserLogin userLogin = userFacade.completeSignUpOwnerUserOAuth(
                new SignUpOwnerUserOAuthCommand(
                        userId,
                        request.businessNumber()
                )
        );

        // 소셜 사용자 인증 업데이트
        SecurityUtils.renewSocialUserAuthentication(userLogin.user(), userLogin.domainId()); //

        // 응답 작성
        SignUpChildResponse response = new SignUpChildResponse(
                userLogin.user().getId(),
                userLogin.user().getLoginId()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 현재 로그인한 사용자 확인
     * GET /api/users/me
     *
     * @return 현재 로그인한 사용자 ID, 로그인 아이디, 사용자 타입, 계정 상태, 계정 타입
     */
    @Override
    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<MeResponse>> getMe() {
        log.info("내 정보 확인 요청");

        Long userId = SecurityUtils.getUserId(); // 세션에 인증된 사용자 ID 획득

        User user = userFacade.getMe(new GetUserByIdQuery(userId)); // 사용자 정보 조회

        MeResponse response = new MeResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(), // 아동은 이름, 사업자는 매장명 반환
                user.getUserType(),
                user.getStatusType(),
                user.getProviderType()
        ); // 응답 생성

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }
}

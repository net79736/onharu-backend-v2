package com.backend.onharu.application;

import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CompleteEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CreateEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.ExistsVerifiedByEmailQuery;
import com.backend.onharu.domain.email.model.EmailAuthentication;
import com.backend.onharu.domain.email.service.EmailAuthenticationCommandService;
import com.backend.onharu.domain.email.service.EmailAuthenticationQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.dto.UserCommand.ResetPasswordUserCommand;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByNameAndPhoneQuery;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserCommandService;
import com.backend.onharu.domain.user.service.UserQueryService;
import com.backend.onharu.infra.email.EmailSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.backend.onharu.domain.support.error.ErrorType.EmailAuthentication.EMAIL_NOT_VERIFIED;
import static com.backend.onharu.domain.user.dto.UserCommand.UpdatePasswordCommand;

/**
 * 사용자의 이메일 인증 및 처리를 수행하는 Facade 입니다.
 */
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final EmailAuthenticationCommandService emailAuthenticationCommandService;
    private final EmailAuthenticationQueryService emailAuthenticationQueryService;
    private final UserQueryService userQueryService;

    private final EmailSendService emailSendService;

    private final PasswordEncoder passwordEncoder;
    private final UserCommandService userCommandService;

    /**
     * 이메일 인증 코드 생성/재인증 메서드
     */
    public void createEmailAuthentication(CreateEmailAuthenticationCommand command) {
        EmailAuthentication emailAuthentication = emailAuthenticationCommandService.create(command); // 이메일 인증 생성

        // 인증 이메일 전송
        emailSendService.sendEmail(
                emailAuthentication.getEmail(),
                emailAuthentication.getToken()
        );
    }

    /**
     * 이메일 인증 완료 메서드
     */
    public void completeEmailAuthentication(CompleteEmailAuthenticationCommand command) {
        emailAuthenticationCommandService.verify(command);
    }

    /**
     * 이메일 인증 확인 메서드
     */
    public void verifyCode(ExistsVerifiedByEmailQuery query) {
        boolean verified = emailAuthenticationQueryService.existsByEmailAndIsVerifiedTrue(query);

        if (!verified) { // 이메일 인증이 안된 경우
            throw new CoreException(EMAIL_NOT_VERIFIED);
        }
    }

    /**
     * 아이디 찾기
     */
    public User findId(GetUserByNameAndPhoneQuery query) {

        User user = userQueryService.getUserByNameAndPhone(query); // 이름, 전화번호를 입력받아서 사용자를 조회합니다

        user.verifyStatus(); // 사용자 계정 상태 검증

        return user;
    }

    /**
     * 비밀번호 찾기
     */
    public void resetPassword(ResetPasswordUserCommand command) { // 파라미터로 필요한거 이메일(로그인 아이디), 이름, 전화번호
        String loginId = command.loginId();
        String name = command.name();
        String phone = command.phone();
        // TODO: 이메일 인증 생성 및 검증은 나중에 상황 보고 추가
        User user = userQueryService.getUserByNameAndPhone(new GetUserByNameAndPhoneQuery(name, phone)); // 이름과 전화번호로 사용자 조회
        user.verifyStatus(); // 사용자 상태 검증

        String tempPassword = createTempPassword(); // 임시 비밀번호 생성
        String encodedPassword = passwordEncoder.encode(tempPassword);// 임시 비밀번호 암호화

        userCommandService.updateUserByIdAndPassword(new UpdatePasswordCommand(user.getId(), encodedPassword));

        emailSendService.sendResetPasswordEmail(loginId, tempPassword);// 임시 비밀번호가 포함된 이메일 발송
    }

    /**
     * 임시 비밀번호 생성 메소드
     */
    private String createTempPassword() {
        String english = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String number = "0123456789";
        String special = "!@#$%^&*()_+";
        String all = english + number + special; // 비밀번호에 사용가능한 모든 문자ㅏ

        SecureRandom random = new SecureRandom();
        char eng = english.charAt(random.nextInt(english.length())); // 랜덤 영문자 1개
        char num = number.charAt(random.nextInt(number.length())); // 랜덤 숫자 1개
        char spe = special.charAt(random.nextInt(special.length())); // 랜덤 특수문자 1개

        List<Character> chars = new ArrayList<>(); // 임시 비밀번호를 담을 배열 생성
        chars.add(eng); // 최소 영문자 1개
        chars.add(num); // 최소 숫자 1개
        chars.add(spe); // 최소 특수문자 1개

        for (int i = 0; i < 13; i++) {
            chars.add(all.charAt(random.nextInt(all.length())));
        }

        Collections.shuffle(chars); // 순서를 랜덤하게 섞음

        StringBuilder sb = new StringBuilder();
        for (char ch : chars) { // Character 를 String 으로 변환
            sb.append(ch);
        }

        return sb.toString();
    }
}

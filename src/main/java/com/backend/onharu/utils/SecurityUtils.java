package com.backend.onharu.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.security.LocalUser;
import com.backend.onharu.infra.security.oauth.SocialUser;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security 관련 유틸리티 클래스
 * ApplicationContextAware를 구현하여 ApplicationContext를 자동으로 주입받습니다.
 */
@Slf4j
@Component
public final class SecurityUtils implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SecurityUtils.applicationContext = applicationContext;
        log.info("SecurityUtils ApplicationContext 초기화 완료");
    }
    
    private SecurityUtils() {
    }
    
    /**
     * 현재 인증된 사용자 ID를 추출합니다.
     * 사용자 타입에 따라 아동인 경우 아동 ID, 사장인 경우 사장 ID를 반환합니다.
     * 
     * @return 사용자 타입에 따른 도메인 ID (아동 ID 또는 사장 ID)
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getCurrentAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        
        User user = null;
        
        // LocalUser 처리 (일반 로그인)
        if (authentication.getPrincipal() instanceof LocalUser localUser) {
            log.info("localUser: {}", localUser.getUser());
            user = localUser.getUser();
        } 
        // SocialUser 처리 (OAuth2 로그인)
        else if (authentication.getPrincipal() instanceof SocialUser socialUser) {
            log.info("socialUser: {}", socialUser.getUser());
            user = socialUser.getUser();
        }
        
        if (user == null) {
            log.info("user가 null");
            return null;
        }
        
        // 사용자 타입에 따라 도메인 ID 반환
        UserType userType = user.getUserType();
        log.info("사용자 타입: {}, 사용자 ID: {}", userType, user.getId());
        
        // ApplicationContext가 초기화되지 않은 경우 null 반환
        if (applicationContext == null) {
            return null;
        }
        
        if (userType == UserType.CHILD) {
            // 아동인 경우 아동 ID 반환
            ChildJpaRepository childRepository = applicationContext.getBean(ChildJpaRepository.class);
            log.info(childRepository.findByUser_Id(user.getId()).toString());
            return childRepository.findByUser_Id(user.getId())
                    .map(child -> child.getId())
                    .orElse(null);
        } else if (userType == UserType.OWNER) {
            // 사장인 경우 사장 ID 반환
            OwnerJpaRepository ownerRepository = applicationContext.getBean(OwnerJpaRepository.class);
            log.info(ownerRepository.findByUser_Id(user.getId()).toString());
            return ownerRepository.findByUser_Id(user.getId())
                    .map(owner -> owner.getId())
                    .orElse(null);
        }

        // ADMIN이나 NONE인 경우 null 반환
        return null;
    }
    
    /**
     * 현재 인증된 사용자의 Authentication 객체를 반환합니다.
     * 
     * @return 현재 Authentication (인증되지 않은 경우 null)
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     * 
     * @return 인증된 경우 true, 그렇지 않으면 false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null 
                && authentication.isAuthenticated() 
                && !authentication.getPrincipal().equals("anonymousUser");
    }
    
    /**
     * 현재 인증된 사용자가 LocalUser인지 확인합니다.
     * 
     * @return LocalUser인 경우 true, 그렇지 않으면 false
     */
    public static boolean isLocalUser() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null 
                && authentication.getPrincipal() instanceof LocalUser;
    }
    
    /**
     * 현재 인증된 사용자가 SocialUser인지 확인합니다.
     * 
     * @return SocialUser인 경우 true, 그렇지 않으면 false
     */
    public static boolean isSocialUser() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null 
                && authentication.getPrincipal() instanceof SocialUser;
    }
    
    /**
     * 현재 인증된 사용자의 LocalUser를 반환합니다.
     * 
     * @return LocalUser (일반 로그인인 경우), 그렇지 않으면 null
     */
    public static LocalUser getCurrentUserDetails() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LocalUser userDetails) {
            return userDetails;
        }
        return null;
    }
    
    /**
     * 현재 인증된 사용자의 CustomOAuth2User를 반환합니다.
     * 
     * @return CustomOAuth2User (OAuth2 로그인인 경우), 그렇지 않으면 null
     */
    public static SocialUser getCurrentOAuth2User() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SocialUser oauth2User) {
            return oauth2User;
        }
        return null;
    }
}


package com.backend.onharu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 테스트 프로파일에서는 SecurityConfig가 로드되지 않아 Spring Boot 기본 보안이 적용되고, 
 * 이 때문에 인증 없는 요청이 302로 리다이렉트됩니다. 
 * TestSecurityConfig는 테스트 때만 로드되어 “모든 경로 허용” 방식으로 보안을 설정하고,
 * 이로 인해 MockMvc 기반 테스트가 인증 없이 200 OK를 받을 수 있습니다.
 */
@Profile("test")
@Configuration
@EnableWebSecurity
class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // From 로그인 방식 disable
                .httpBasic(AbstractHttpConfigurer::disable) // Basic 인증 방식 disable
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 모든 경로 허용

        return http.build();
    }
}

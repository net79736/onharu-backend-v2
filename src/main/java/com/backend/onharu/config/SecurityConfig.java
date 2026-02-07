package com.backend.onharu.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.infra.security.oauth.SocialUserService;
import com.backend.onharu.infra.security.oauth.handler.OAuth2FailureHandler;
import com.backend.onharu.infra.security.oauth.handler.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String[] PUBLIC_PATH = {
            "/", "/oauth2/**",
            "/users/login/**", "/users/signup/**",
            "/api-docs/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**",
            "/error", "/favicon.ico",
            "/levels/**", 
            "/childrens/**",
            "/owners/**",
            "/admins/**",
            "/stores/**",
            "/store-schedules/**"
    };

    public static final String[] AUTHENTICATE_PATH = {
            "/users/logout/**", "/users/me/**"
    };

    public static final String[] ROLE_CHILD_PATH = {
            "/children/**"
    };

    public static final String[] ROLE_OWNER_PATH = {
            "/owners/**"
    };

    public static final String[] ROLE_ADMIN_PATH = {
            "/admins/**"
    };

    private static final String PORT_FRONT_LOCAL = "http://localhost:5173";

    private final SocialUserService socialUserService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(PORT_FRONT_LOCAL));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setMaxAge(6000L); // 100분

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2SuccessHandler oAuth2SuccessHandler, OAuth2FailureHandler oAuth2FailureHandler) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
        ;

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATH).permitAll()
                        .requestMatchers(AUTHENTICATE_PATH).authenticated()
                        .requestMatchers(ROLE_CHILD_PATH).hasRole(UserType.CHILD.name())
                        .requestMatchers(ROLE_OWNER_PATH).hasRole(UserType.OWNER.name())
                        .requestMatchers(ROLE_ADMIN_PATH).hasRole(UserType.ADMIN.name())
                )
        ;

        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(socialUserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
        ;

        return http.build();
    }
}

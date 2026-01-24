package com.backend.onharu.interfaces.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger/OpenAPI 설정
 * 
 * API 문서화를 위한 Swagger UI 설정입니다.
 * 세션 기반 인증을 사용하며, Swagger UI에서 쿠키 기반 인증을 지원합니다.
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * OpenAPI 설정을 커스터마이징하는 Bean을 정의
     * 
     * 세션 기반 인증을 위해 쿠키 기반 SecurityScheme을 추가합니다.
     * Swagger UI에서 로그인 후 JSESSIONID 쿠키가 자동으로 전송됩니다.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String basicAuthSchemeName = "basicAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("온하루 API")
                        .description("온하루 프로젝트의 REST API 문서입니다.\n\n" +
                                "## 인증 방법\n\n" +
                                "### 방법 1: Swagger UI Authorize 버튼 사용 (권장)\n" +
                                "1. 상단의 **Authorize** 버튼을 클릭합니다.\n" +
                                "2. **basicAuth** 섹션에서 이메일과 비밀번호를 입력합니다.\n" +
                                "   - Username: 사용자 이메일 (예: user@example.com)\n" +
                                "   - Password: 사용자 비밀번호\n" +
                                "3. **Authorize** 버튼을 클릭하여 인증합니다.\n" +
                                "4. 인증 성공 시 세션 쿠키가 자동으로 설정되어 모든 API 호출에 사용됩니다.\n\n" +
                                "### 방법 2: 로그인 API 사용\n" +
                                "1. `[개발 예정]` API를 사용하여 로그인합니다.\n" +
                                "2. 로그인 성공 시 세션 쿠키(JSESSIONID)가 자동으로 설정됩니다.\n" +
                                "3. 이후 모든 인증이 필요한 API 요청에 세션 쿠키가 자동으로 포함됩니다.\n\n" +
                                "**참고**: 세션 쿠키는 브라우저가 자동으로 관리하므로, 한 번 로그인하면 " +
                                "브라우저를 닫기 전까지 인증 상태가 유지됩니다.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("온하루 팀")
                                .email("support@onharu.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        // HTTP Basic 인증 (Swagger UI에서 아이디/패스워드 입력용)
                        .addSecuritySchemes(basicAuthSchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("이메일과 비밀번호를 입력하여 로그인합니다. " +
                                            "Username에는 이메일 주소를 입력하세요.")))
                // 기본적으로 세션 쿠키 인증을 사용 (Basic 인증은 Swagger UI에서만 사용)
                .addSecurityItem(new SecurityRequirement().addList(basicAuthSchemeName));
    }
}

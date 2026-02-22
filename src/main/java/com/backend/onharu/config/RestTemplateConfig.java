package com.backend.onharu.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * restTemplate 빈 등록 코드 입니다.
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    @Value("${external.nts.connect-timeout}")
    private int connectTimeout; // 서버 연결 대기 시간

    @Value("${external.nts.read-timeout}")
    private int readTimeout; // 응답 대기 시간


    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }
}

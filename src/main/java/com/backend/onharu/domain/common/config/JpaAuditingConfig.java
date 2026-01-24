package com.backend.onharu.domain.common.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * 
 * <h3>동작 방식:</h3>
 * <ol>
 *   <li>엔티티 저장 시 현재 시간을 UTC로 변환</li>
 *   <li>@CreatedDate, @LastModifiedDate가 UTC 시간으로 자동 설정</li>
 *   <li>데이터베이스에 UTC 시간 저장</li>
 * </ol>
 */
@EnableJpaAuditing(
    modifyOnCreate = false, 
    auditorAwareRef = "auditorAware",
    dateTimeProviderRef = "kstDateTimeProvider"
)
@Configuration
public class JpaAuditingConfig {
    
    @Bean(name = "auditorAware")
	public AuditorAware<String> auditorAware() {
		return new SecurityAuditorAware();
	}

    /**
     * KST 시간을 제공하는 DateTimeProvider 빈
     * 
     * <p>JPA Auditing에서 사용할 날짜/시간을 KST로 제공합니다.</p>
     * 
     * <h3>사용 예시:</h3>
     * <pre>{@code
     * // 엔티티 저장 시
     * Post post = new Post(...);
     * postRepository.save(post);
     * // createdAt이 KST 시간으로 자동 설정됨
     * }</pre>
     * 
     * @return KST 시간을 제공하는 DateTimeProvider
     */
    @Bean(name = "kstDateTimeProvider")
    public DateTimeProvider kstDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }
}

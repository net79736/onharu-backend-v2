package com.backend.onharu.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
@Profile({"dev", "test"})
public class S3ConfigLocal {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;

    /** S3 서명용 리전 */
    @Value("${minio.region}")
    private String minioRegion;

    @PostConstruct
    public void printMinioUrl() {
        log.info("minioUrl: " + minioUrl);
        log.info("minioAccessKey: " + minioAccessKey);
        log.info("minioSecretKey: " + minioSecretKey);
        log.info("minioRegion: " + minioRegion);
    }


    /**
     * MinIO URL 반환
     *
     * @return MinIO 서버 URL
     */
    public String getMinioUrl() {
        return minioUrl;
    }

    /**
     * MinIO 클라이언트 빈 생성
     *
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client minioClient() {
        try {
            // path-style은 S3Configuration 한 곳에서만 설정 (SDK 2.x는 forcePathStyle + pathStyleAccessEnabled 동시 설정 시 예외)
            return S3Client.builder()
                    .endpointOverride(URI.create(minioUrl))
                    .region(Region.of(minioRegion))
                    .credentialsProvider(StaticCredentialsProvider
                            .create(AwsBasicCredentials
                                    .create(minioAccessKey, minioSecretKey)))
                    .serviceConfiguration(
                            S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build()
                    )
                    .build();
        } catch (Exception e) {
            log.warn("MinIO S3Client 빌드 실패: url={}, region={}, cause={}", minioUrl, minioRegion, e.getMessage());
            throw new CoreException(ErrorType.FileOperation.MINIO_CLIENT_ERROR);
        }
    }

    /**
     * MinIO Presigner 빈 생성
     *
     * @return S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner minioS3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(minioUrl))
                .region(Region.of(minioRegion))
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create(minioAccessKey, minioSecretKey)))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .build();
    }
}

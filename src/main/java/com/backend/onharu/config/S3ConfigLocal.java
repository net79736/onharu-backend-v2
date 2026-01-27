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

    @Value("${minio.bucket}")
    private String region;

    @PostConstruct
    public void printMinioUrl() {
        log.info("minioUrl: " + minioUrl);
        log.info("minioAccessKey: " + minioAccessKey);
        log.info("minioSecretKey: " + minioSecretKey);
        log.info("region: " + region);
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
            return S3Client.builder()
                    .endpointOverride(URI.create(minioUrl))
                    .region(Region.of(region))
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
                .region(Region.of(region))
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

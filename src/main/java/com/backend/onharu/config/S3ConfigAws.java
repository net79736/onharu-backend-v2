package com.backend.onharu.config;

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
@Profile({"prod"})
public class S3ConfigAws {

    @Value("${aws.s3.access-key}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secret-key}")
    private String awsS3SecretKey;

    @Value("${aws.s3.region}")
    private String awsS3region;

    @PostConstruct
    public void printMinioUrl() {
        log.info("awsAccessKey: " + awsS3AccessKey);
        log.info("awsSecretKey: " + awsS3SecretKey);
        log.info("awsS3region: " + awsS3region);
    }

    /**
     * AWS S3 클라이언트 빈 생성
     *
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client awsS3Client() {
        try {
            return S3Client.builder()
                    .region(Region.of(awsS3region))
                    .credentialsProvider(StaticCredentialsProvider
                            .create(AwsBasicCredentials
                                    .create(awsS3AccessKey, awsS3SecretKey)))
                    .serviceConfiguration(
                            S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build()
                    )
                    .build();
        } catch (Exception e) {
            throw new CoreException(ErrorType.FileOperation.AWS_S3_CLIENT_ERROR);
        }
    }

    /**
     * S3 Presigner 빈 생성
     *
     * @return S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(awsS3region))
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create(awsS3AccessKey, awsS3SecretKey)))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .build();
    }
}

package com.cc.qylgjavaservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class R2Config {
    @Value("${cloudflare.r2.cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.cloudflare.r2.access-key}")
    private String accessKeyId;

    @Value("${cloudflare.r2.cloudflare.r2.secret-key}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        // R2 的 Endpoint 格式
        String endpointUrl = "https://" + accountId + ".r2.cloudflarestorage.com";

        return S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of("auto")) // R2 必须设置为 auto
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // 重要：R2 通常需要使用 path style (bucket 在路径中)
                        .build())
                .build();
    }
}

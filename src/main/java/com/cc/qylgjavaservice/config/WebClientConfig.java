package com.cc.qylgjavaservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${ai.service.uri}")
    private String fastApiUri;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(fastApiUri)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}

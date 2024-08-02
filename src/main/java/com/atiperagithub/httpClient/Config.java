package com.atiperagithub.httpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class Config {

    @Value("${service.github.url}")
    private String gitHubUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(gitHubUrl)
                .build();
    }
}

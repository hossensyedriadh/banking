package io.github.hossensyedriadh.banking_api_gateway.configuration.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
    @Bean("iamWebClient")
    public WebClient iamWebClient() {
        return WebClient.builder().build();
    }
}

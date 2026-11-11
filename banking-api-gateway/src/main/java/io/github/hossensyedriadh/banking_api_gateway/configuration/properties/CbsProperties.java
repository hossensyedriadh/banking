package io.github.hossensyedriadh.banking_api_gateway.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "banking.cbs")
public record CbsProperties(URI baseUrl) {
}

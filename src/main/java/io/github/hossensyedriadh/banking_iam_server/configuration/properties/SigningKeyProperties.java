package io.github.hossensyedriadh.banking_iam_server.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("banking.iam.signing")
public record SigningKeyProperties(String keystore, String storePassword, String keyPassword, String alias,
                                   String keyId) {
}

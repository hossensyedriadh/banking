package io.github.hossensyedriadh.internet_banking.configuration.security;

import io.github.hossensyedriadh.internet_banking.configuration.security.encryption.EncryptedJdbcOAuth2AuthorizedClientService;
import io.github.hossensyedriadh.internet_banking.configuration.security.encryption.TokenEncryptionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class OAuth2AuthorizedClientConfiguration {
    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(JdbcOperations jdbcOperations, ClientRegistrationRepository clientRegistrationRepository,
                                                                       TokenEncryptionService tokenEncryptionService) {
        return new EncryptedJdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository, tokenEncryptionService);
    }

    @Bean
    public OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                       OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        OAuth2AuthorizedClientProvider clientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode().refreshToken().build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                oAuth2AuthorizedClientService);
        clientManager.setAuthorizedClientProvider(clientProvider);

        return clientManager;
    }

    @Bean
    @Qualifier("backgroundAuthorizedClientManager")
    public OAuth2AuthorizedClientManager backgroundAuthorizedClientManager(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService authorizedClientService) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials().build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService         );
        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }
}

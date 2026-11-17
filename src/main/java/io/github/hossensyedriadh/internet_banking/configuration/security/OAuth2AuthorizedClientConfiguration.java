package io.github.hossensyedriadh.internet_banking.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class OAuth2AuthorizedClientConfiguration {
    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(JdbcOperations jdbcOperations,
                                                                       ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
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
}

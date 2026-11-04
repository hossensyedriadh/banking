package io.github.hossensyedriadh.banking_iam_server.configuration.client;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Log4j2
@Configuration
public class UserClientConfiguration {
    private final RegisteredClientRepository clientRepository;

    @Autowired
    public UserClientConfiguration(RegisteredClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Bean
    public ApplicationRunner registerInternetBankingClient(
            @Qualifier("clientPwdEncoder") PasswordEncoder passwordEncoder,
            @Value("${banking.iam.clients.internet-banking.secret}") String clientSecret,
            @Value("${banking.iam.clients.internet-banking.redirect-uri}") String redirectUri,
            @Value("${banking.iam.clients.internet-banking.post-logout-redirect-uri}") String postLogoutRedirectUri) {

        return _ -> {
            RegisteredClient existingClient = this.clientRepository.findByClientId("internet-banking");

            RegisteredClient.Builder clientBuilder = existingClient == null ? RegisteredClient.withId(UUID.randomUUID().toString()) : RegisteredClient.from(existingClient);

            RegisteredClient internetBankingClient = clientBuilder.clientId("internet-banking")
                    .clientName("Internet Banking")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantTypes(grantTypes -> {
                        grantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                        grantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
                    })
                    .redirectUri(redirectUri)
                    .scopes(scopes -> {
                        scopes.add(OidcScopes.OPENID);
                        scopes.add(OidcScopes.PROFILE);
                        scopes.add(OidcScopes.EMAIL);
                        scopes.add(OidcScopes.PHONE);
                    })
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .clientSettings(ClientSettings.builder().requireProofKey(true)
                            .requireAuthorizationConsent(true)
                            .build())
                    .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(5))
                            .refreshTokenTimeToLive(Duration.ofDays(3))
                            .reuseRefreshTokens(false)
                            .build())
                    .postLogoutRedirectUri(postLogoutRedirectUri)
                    .build();

            if (existingClient == null) {
                log.info("Saving new OAuth2 client: {}", internetBankingClient.getClientId());
                this.clientRepository.save(internetBankingClient);
            } else {
                log.info("Updating existing OAuth2 client: {}", internetBankingClient.getClientId());
                this.clientRepository.save(internetBankingClient);
            }
        };
    }
}

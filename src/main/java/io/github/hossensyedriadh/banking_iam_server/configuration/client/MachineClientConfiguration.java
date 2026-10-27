package io.github.hossensyedriadh.banking_iam_server.configuration.client;

import io.github.hossensyedriadh.banking_iam_server.util.OAuth2Scopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Configuration
public class MachineClientConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner registerAtmPostSwitchClient(RegisteredClientRepository clientRepository, PasswordEncoder passwordEncoder,
                                                         @Value("${banking.iam.clients.atm-pos-switch.secret}") String clientSecret) {
        return _ -> {
            if (clientRepository.findByClientId("atm-pos-switch") != null) {
                return;
            }

            RegisteredClient switchClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("atm-pos-switch").clientName("ATM/POS Switch")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .scopes(scopes -> scopes.addAll(Set.of(OAuth2Scopes.ATM_BALANCE_READ, OAuth2Scopes.ATM_ACCOUNT_INFO_READ,
                            OAuth2Scopes.ATM_CARD_INFO_READ, OAuth2Scopes.ATM_CARD_PIN_VERIFY, OAuth2Scopes.ATM_CURRENCY_INFO_READ,
                            OAuth2Scopes.ATM_ACCOUNT_PRODUCT_INFO_READ, OAuth2Scopes.ATM_WITHDRAW_CREATE, OAuth2Scopes.ATM_REVERSAL_CREATE,
                            OAuth2Scopes.ATM_TRANSACTIONS_SEARCH_READ, OAuth2Scopes.ATM_TRANSACTION_READ)))
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(5))
                            .build())
                    .build();

            clientRepository.save(switchClient);
        };
    }
}

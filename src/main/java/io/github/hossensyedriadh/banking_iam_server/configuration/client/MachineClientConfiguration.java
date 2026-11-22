package io.github.hossensyedriadh.banking_iam_server.configuration.client;

import io.github.hossensyedriadh.banking_iam_server.util.AtmOAuth2Scopes;
import io.github.hossensyedriadh.banking_iam_server.util.CbsOAuth2Scopes;
import io.github.hossensyedriadh.banking_iam_server.util.InternetBankingOAuth2Scopes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class MachineClientConfiguration {
    private final RegisteredClientRepository clientRepository;

    @Autowired
    public MachineClientConfiguration(RegisteredClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Bean
    public ApplicationRunner registerAtmPostSwitchClient(@Qualifier("clientPwdEncoder") PasswordEncoder passwordEncoder, @Value("${banking.iam.clients.atm-pos-switch.secret}") String clientSecret,
                                                         @Value("${banking.iam.clients.atm-pos-switch.access-token.ttl}") Duration accessTokenTtl) {
        return _ -> {
            RegisteredClient existingClient = this.clientRepository.findByClientId("atm-pos-switch");

            RegisteredClient.Builder builder = existingClient == null ? RegisteredClient.withId(UUID.randomUUID().toString()) : RegisteredClient.from(existingClient);

            RegisteredClient switchClient = builder.clientId("atm-pos-switch")
                    .clientName("ATM/POS Switch")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .scopes(scopes -> scopes.addAll(AtmOAuth2Scopes.all()))
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(accessTokenTtl)
                            .build())
                    .build();

            this.clientRepository.save(switchClient);
        };
    }

    @Bean
    public ApplicationRunner registerGatewayClient(@Qualifier("clientPwdEncoder") PasswordEncoder passwordEncoder, @Value("${banking.iam.clients.banking-api-gateway.secret}") String clientSecret,
                                                   @Value("${banking.iam.clients.banking-api-gateway.access-token.ttl}") Duration accessTokenTtl) {
        return _ -> {
            RegisteredClient existingClient = this.clientRepository.findByClientId("banking-api-gateway");

            RegisteredClient.Builder builder = existingClient == null ? RegisteredClient.withId(UUID.randomUUID().toString()) : RegisteredClient.from(existingClient);

            RegisteredClient gatewayClient = builder.clientId("banking-api-gateway")
                    .clientName("Banking API Gateway")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantTypes(types -> {
                        types.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
                        types.add(AuthorizationGrantType.TOKEN_EXCHANGE);
                    })
                    .scopes(scopes -> scopes.addAll(CbsOAuth2Scopes.all()))
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(accessTokenTtl)
                            .build())
                    .build();

            this.clientRepository.save(gatewayClient);
        };
    }

    @Bean
    public ApplicationRunner registerInternetBankingServiceClient(@Qualifier("clientPwdEncoder") PasswordEncoder passwordEncoder, @Value("${banking.iam.clients.internet-banking-service.secret}") String clientSecret,
                                                                  @Value("${banking.iam.clients.internet-banking-service.access-token.ttl}") Duration accessTokenTtl) {
        return _ -> {
            RegisteredClient existingClient = this.clientRepository.findByClientId("internet-banking-service");

            RegisteredClient.Builder builder = existingClient == null ? RegisteredClient.withId(UUID.randomUUID().toString()) : RegisteredClient.from(existingClient);

            RegisteredClient iBankingServiceClient = builder.clientId("internet-banking-service")
                    .clientName("Internet Banking Service")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .scopes(scopes -> scopes.addAll(InternetBankingOAuth2Scopes.forService()))
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(accessTokenTtl)
                            .build())
                    .build();

            this.clientRepository.save(iBankingServiceClient);
        };
    }
}

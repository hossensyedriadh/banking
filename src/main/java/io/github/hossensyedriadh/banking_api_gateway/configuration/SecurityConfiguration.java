package io.github.hossensyedriadh.banking_api_gateway.configuration;

import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.AtmEndpoints;
import io.github.hossensyedriadh.banking_api_gateway.util.OAuth2Scopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.pathMatchers("/error", "/actuator/health/**",
                                "actuator/info").permitAll()
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/accounts/*/balance"
                        ).hasAuthority(OAuth2Scopes.ATM_BALANCE_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/accounts/*"
                        ).hasAuthority(OAuth2Scopes.ATM_ACCOUNT_INFO_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/account-products/*"
                        ).hasAuthority(OAuth2Scopes.ATM_ACCOUNT_PRODUCT_INFO_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/currencies/*"
                        ).hasAuthority(OAuth2Scopes.ATM_CURRENCY_INFO_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/cards/*"
                        ).hasAuthority(OAuth2Scopes.ATM_CARD_INFO_READ)
                        .pathMatchers(HttpMethod.POST, AtmEndpoints.CARD_PIN_VERIFICATION
                        ).hasAuthority(OAuth2Scopes.ATM_CARD_PIN_VERIFY)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.TRANSACTIONS_SEARCH
                        ).hasAuthority(OAuth2Scopes.ATM_TRANSACTIONS_SEARCH_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/transactions/idempotency/*"
                        ).hasAuthority(OAuth2Scopes.ATM_TRANSACTION_READ)
                        .pathMatchers(HttpMethod.POST, AtmEndpoints.TRANSACTION_WITHDRAWAL
                        ).hasAuthority(OAuth2Scopes.ATM_WITHDRAW_CREATE)
                        .pathMatchers(HttpMethod.POST, AtmEndpoints.TRANSACTION_REVERSAL
                        ).hasAuthority(OAuth2Scopes.ATM_REVERSAL_CREATE)
                        .pathMatchers(AtmEndpoints.BASE + "/**").denyAll()
                        .anyExchange().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2.jwt(_ -> {
                })).build();
    }
}

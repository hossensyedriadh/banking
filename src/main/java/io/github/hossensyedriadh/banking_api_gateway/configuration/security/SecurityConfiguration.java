package io.github.hossensyedriadh.banking_api_gateway.configuration.security;

import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.AtmEndpoints;
import io.github.hossensyedriadh.banking_api_gateway.util.AtmOAuth2Scopes;
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
                                "/actuator/info",  "/cbs/docs", "/cbs/docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/accounts/*/balance"
                        ).hasAuthority(AtmOAuth2Scopes.ATM_BALANCE_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/accounts/*"
                        ).hasAuthority(AtmOAuth2Scopes.ATM_ACCOUNT_INFO_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/account-products/*"
                        ).hasAuthority(AtmOAuth2Scopes.ATM_ACCOUNT_PRODUCT_INFO_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/currencies/*"
                        ).hasAuthority(AtmOAuth2Scopes.ATM_CURRENCY_INFO_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/cards/*"
                        ).hasAuthority(AtmOAuth2Scopes.ATM_CARD_INFO_READ)
                        .pathMatchers(HttpMethod.POST, AtmEndpoints.CARD_PIN_VERIFICATION
                        ).hasAuthority(AtmOAuth2Scopes.ATM_CARD_PIN_VERIFY)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.TRANSACTIONS_SEARCH
                        ).hasAuthority(AtmOAuth2Scopes.ATM_TRANSACTIONS_SEARCH_READ)
                        .pathMatchers(HttpMethod.GET, AtmEndpoints.BASE + "/transactions/idempotency/*"
                        ).hasAuthority(AtmOAuth2Scopes.ATM_TRANSACTION_READ)
                        .pathMatchers(HttpMethod.POST, AtmEndpoints.TRANSACTION_WITHDRAWAL
                        ).hasAuthority(AtmOAuth2Scopes.ATM_WITHDRAW_CREATE)
                        .pathMatchers(HttpMethod.POST, AtmEndpoints.TRANSACTION_REVERSAL
                        ).hasAuthority(AtmOAuth2Scopes.ATM_REVERSAL_CREATE)
                        .pathMatchers(AtmEndpoints.BASE + "/**").denyAll()
                        .anyExchange().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2.jwt(_ -> {
                })).build();
    }
}

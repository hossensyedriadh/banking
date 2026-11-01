package io.github.hossensyedriadh.banking_api_gateway.tokenexchange;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class DelegatedTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<DelegatedTokenGatewayFilterFactory.Config> {
    private final DelegatedTokenService delegatedTokenService;

    @Autowired
    public DelegatedTokenGatewayFilterFactory(DelegatedTokenService delegatedTokenService) {
        super(Config.class);
        this.delegatedTokenService = delegatedTokenService;
    }

    @Override
    @NonNull
    public GatewayFilter apply(@NonNull Config config) {
        return (exchange, chain) -> exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated Switch JWT is required")))
                .flatMap(authentication -> {
                    boolean hasRequiredAuthority = authentication.getAuthorities().stream()
                            .anyMatch(authority -> Objects.equals(authority.getAuthority(), config.getRequiredAuthority()));

                    if (!hasRequiredAuthority) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Required ATM authority is missing"));
                    }

                    return this.delegatedTokenService.exchange(authentication.getToken().getTokenValue(), config.getRequiredAuthority());
                }).flatMap(delegatedToken -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().headers(headers -> headers.setBearerAuth(delegatedToken.accessToken())).build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("requiredAuthority");
    }

    public static class Config {

        private String requiredAuthority;

        public Config() {
        }

        public Config(String requiredAuthority) {
            this.requiredAuthority = requiredAuthority;
        }

        public String getRequiredAuthority() {
            return requiredAuthority;
        }

        public void setRequiredAuthority(String requiredAuthority) {
            this.requiredAuthority = requiredAuthority;
        }
    }
}

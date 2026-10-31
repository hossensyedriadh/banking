package io.github.hossensyedriadh.banking_iam_server.authentication.tokenexchange;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenExchangeAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TokenExchangePolicyAuthenticationProvider implements AuthenticationProvider {
    private final TokenExchangePolicy tokenExchangePolicy;

    @Autowired
    public TokenExchangePolicyAuthenticationProvider(TokenExchangePolicy tokenExchangePolicy) {
        this.tokenExchangePolicy = tokenExchangePolicy;
    }

    @Override
    public @Nullable Authentication authenticate(@NonNull Authentication authentication) throws OAuth2AuthenticationException {
        if (!(authentication instanceof OAuth2TokenExchangeAuthenticationToken exchange)) {
            return null;
        }

        if (!(exchange.getPrincipal() instanceof OAuth2ClientAuthenticationToken clientAuthentication)
                || !clientAuthentication.isAuthenticated() || clientAuthentication.getRegisteredClient() == null) {
            throw new OAuth2AuthenticationException("invalid_client");
        }

        this.tokenExchangePolicy.validate(exchange, clientAuthentication.getRegisteredClient());

        return null;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return OAuth2TokenExchangeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

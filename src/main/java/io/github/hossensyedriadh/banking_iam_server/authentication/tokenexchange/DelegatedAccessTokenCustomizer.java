package io.github.hossensyedriadh.banking_iam_server.authentication.tokenexchange;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenExchangeActor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenExchangeCompositeAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DelegatedAccessTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    @Override
    public void customize(@NonNull JwtEncodingContext context) {
        if (!AuthorizationGrantType.TOKEN_EXCHANGE.equals(context.getAuthorizationGrantType())) {
            return;
        }

        Authentication principal = context.getPrincipal();

        if (!(principal instanceof OAuth2TokenExchangeCompositeAuthenticationToken compositeToken)) {
            throw new IllegalStateException("Expected a token exchange composite authentication principal");
        }

        Authentication subject = compositeToken.getSubject();

        List<OAuth2TokenExchangeActor> actors = compositeToken.getActors();

        if (actors.isEmpty()) {
            throw new IllegalStateException("Token exchange actor is missing");
        }

        OAuth2TokenExchangeActor latestActor = actors.getLast();

        context.getClaims().subject(subject.getName()).audience(List.of("cbs"))
                .claim("act", latestActor.getClaims());
    }
}

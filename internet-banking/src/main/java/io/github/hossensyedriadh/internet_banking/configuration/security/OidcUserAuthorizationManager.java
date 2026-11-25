package io.github.hossensyedriadh.internet_banking.configuration.security;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class OidcUserAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Override
    public @Nullable AuthorizationResult authorize(@NonNull Supplier<? extends @Nullable Authentication> authenticationSupplier, RequestAuthorizationContext object) {
        Authentication authentication = authenticationSupplier.get();

        boolean authorized = authentication.isAuthenticated()
                && ((authentication instanceof OAuth2AuthenticationToken && authentication.getPrincipal() instanceof OidcUser)
                || authentication instanceof JwtAuthenticationToken);

        return new AuthorizationDecision(authorized);
    }
}

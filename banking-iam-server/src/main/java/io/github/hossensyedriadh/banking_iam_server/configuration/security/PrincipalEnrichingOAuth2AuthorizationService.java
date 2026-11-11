package io.github.hossensyedriadh.banking_iam_server.configuration.security;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.security.Principal;

public class PrincipalEnrichingOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private final OAuth2AuthorizationService delegate;

    public PrincipalEnrichingOAuth2AuthorizationService(OAuth2AuthorizationService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        if (!AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authorization.getAuthorizationGrantType())) {
            this.delegate.save(authorization);
            return;
        }

        if (authorization.getAttribute(Principal.class.getName()) != null) {
            this.delegate.save(authorization);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2ClientAuthenticationToken clientAuthentication)
                || !clientAuthentication.isAuthenticated() || clientAuthentication.getRegisteredClient() == null) {
            throw new IllegalStateException("Authenticated OAuth2 client principal is unavailable");
        }

        RegisteredClient registeredClient = clientAuthentication.getRegisteredClient();

        if (!authorization.getRegisteredClientId().equals(registeredClient.getId())
                || !authorization.getPrincipalName().equals(clientAuthentication.getName())) {
            throw new IllegalStateException("Authenticated client does not match authorization");
        }

        OAuth2Authorization enrichedAuthorization = OAuth2Authorization.from(authorization)
                .attribute(Principal.class.getName(), new PersistentClientPrincipal(clientAuthentication.getName()))
                .build();

        this.delegate.save(enrichedAuthorization);
    }

    @Override
    public void remove(@NonNull OAuth2Authorization authorization) {
        this.delegate.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(@NonNull String id) {
        return this.delegate.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(@NonNull String token, OAuth2TokenType tokenType) {
        return this.delegate.findByToken(token, tokenType);
    }
}

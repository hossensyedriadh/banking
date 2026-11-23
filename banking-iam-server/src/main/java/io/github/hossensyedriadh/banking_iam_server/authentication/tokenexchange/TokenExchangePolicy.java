package io.github.hossensyedriadh.banking_iam_server.authentication.tokenexchange;

import io.github.hossensyedriadh.banking_iam_server.enumerator.ChannelType;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenExchangeAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Log4j2
@Component
public class TokenExchangePolicy {
    private static final String ATM_POS_SWITCH_CLIENT_ID = "atm-pos-switch";
    private static final String GATEWAY_CLIENT_ID = "banking-api-gateway";
    private static final String CBS_AUDIENCE = "cbs";

    private static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";

    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository clientRepository;

    public TokenExchangePolicy(OAuth2AuthorizationService authorizationService, RegisteredClientRepository clientRepository) {
        this.authorizationService = authorizationService;
        this.clientRepository = clientRepository;
    }

    public void validate(OAuth2TokenExchangeAuthenticationToken exchange, RegisteredClient authenticatedClient) {
        this.validateAuthenticatedClient(authenticatedClient);
        this.validateTokenTypes(exchange);

        OAuth2Authorization subjectAuthorization = this.findActiveAuthorization(exchange.getSubjectToken());

        OAuth2Authorization actorAuthorization = this.findActiveAuthorization(exchange.getActorToken());

        this.validateRegisteredClient(subjectAuthorization, ATM_POS_SWITCH_CLIENT_ID);
        this.validateRegisteredClient(actorAuthorization, GATEWAY_CLIENT_ID);

        this.validateSubject(subjectAuthorization);
        this.validateActor(actorAuthorization);
        this.validateAudience(subjectAuthorization);
        this.validateRequestedAudience(exchange.getAudiences().stream().toList());

        ChannelType channelType = this.resolveChannelType(subjectAuthorization);

        this.validateRequestedScopes(subjectAuthorization, channelType, exchange.getScopes());
    }

    private void validateAuthenticatedClient(RegisteredClient client) {
        if (!GATEWAY_CLIENT_ID.equals(client.getClientId()) || !client.getAuthorizationGrantTypes().contains(AuthorizationGrantType.TOKEN_EXCHANGE)) {
            throw this.error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, "Client is not permitted to perform token exchange");
        }
    }

    private void validateTokenTypes(OAuth2TokenExchangeAuthenticationToken exchange) {
        if (!ACCESS_TOKEN_TYPE.equals(exchange.getSubjectTokenType()) || !ACCESS_TOKEN_TYPE.equals(exchange.getActorTokenType())
                || !ACCESS_TOKEN_TYPE.equals(exchange.getRequestedTokenType())) {
            throw this.error(OAuth2ErrorCodes.INVALID_REQUEST, "Only access token exchange is supported");
        }

        if (!StringUtils.hasText(exchange.getActorToken())) {
            throw this.error(OAuth2ErrorCodes.INVALID_REQUEST, "Actor token is required");
        }
    }

    private OAuth2Authorization findActiveAuthorization(String tokenValue) {
        if (!StringUtils.hasText(tokenValue)) {
            throw this.error(OAuth2ErrorCodes.INVALID_GRANT, "Token is missing");
        }

        OAuth2Authorization authorization = this.authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);

        if (authorization == null || authorization.getAccessToken() == null || !authorization.getAccessToken().isActive()) {
            throw this.error(OAuth2ErrorCodes.INVALID_GRANT, "Token is invalid or inactive");
        }

        return authorization;
    }

    private void validateRegisteredClient(OAuth2Authorization authorization, String expectedClientId) {
        RegisteredClient registeredClient = this.clientRepository.findById(authorization.getRegisteredClientId());

        if (registeredClient == null || !expectedClientId.equals(registeredClient.getClientId())) {
            throw this.error(OAuth2ErrorCodes.INVALID_GRANT, "Token belongs to an unexpected client");
        }
    }

    private void validateSubject(OAuth2Authorization authorization) {
        if (!ATM_POS_SWITCH_CLIENT_ID.equals(authorization.getPrincipalName())) {
            throw this.error(OAuth2ErrorCodes.INVALID_GRANT, "Invalid subject token principal");
        }
    }

    private void validateActor(OAuth2Authorization authorization) {
        Map<String, Object> claims = Objects.requireNonNull(authorization.getAccessToken()).getClaims();

        if (claims == null || !GATEWAY_CLIENT_ID.equals(claims.get("sub"))) {
            throw this.error(OAuth2ErrorCodes.INVALID_GRANT, "Invalid actor identity");
        }
    }

    private void validateAudience(OAuth2Authorization authorization) {
        Map<String, Object> claims = Objects.requireNonNull(authorization.getAccessToken()).getClaims();

        Object audience = claims != null ? claims.get("aud") : null;

        boolean validAudience;

        if (audience instanceof String value) {
            validAudience = GATEWAY_CLIENT_ID.equals(value);
        } else if (audience instanceof Collection<?> values) {
            validAudience = values.contains(GATEWAY_CLIENT_ID);
        } else {
            validAudience = false;
        }

        if (!validAudience) {
            throw this.error(OAuth2ErrorCodes.INVALID_GRANT, "Invalid subject token audience");
        }
    }

    private void validateRequestedAudience(List<String> audiences) {
        if (audiences == null || audiences.size() != 1 || !CBS_AUDIENCE.equals(audiences.getFirst())) {
            throw this.error("invalid_target", "Only cbs audience is permitted");
        }
    }

    private void validateRequestedScopes(OAuth2Authorization subjectAuthorization, ChannelType channelType, Set<String> requestedScopes) {
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            throw this.error(OAuth2ErrorCodes.INVALID_SCOPE, "At least one CBS scope is required");
        }

        Map<String, Object> claims = Objects.requireNonNull(subjectAuthorization.getAccessToken()).getClaims();

        Object scopeClaim = claims != null ? claims.get("scope") : null;

        if (!(scopeClaim instanceof Collection<?> subjectScopes)) {
            throw this.error(OAuth2ErrorCodes.INVALID_GRANT, "Subject token has no valid scope claim");
        }

        Set<String> scopes = subjectScopes.stream().filter(String.class::isInstance).map(String.class::cast).collect(Collectors.toSet());

        Set<String> allowedCbsScopes = TokenExchangeScopeMappings.allowedCbsScopes(channelType, scopes);

        if (!allowedCbsScopes.containsAll(requestedScopes)) {
            throw this.error(OAuth2ErrorCodes.INVALID_SCOPE, "Requested CBS scopes exceed " + channelType + " permissions");
        }
    }

    private ChannelType resolveChannelType(OAuth2Authorization subjectAuthorization) {
        String clientId = subjectAuthorization.getRegisteredClientId();

        return switch (clientId) {
            case "atm-pos-switch" -> ChannelType.SWITCH;
            case "internet-banking" -> ChannelType.INTERNET_BANKING;
            case "internet-banking-service" -> ChannelType.INTERNET_BANKING_SERVICE;
            default -> throw this.error(
                    OAuth2ErrorCodes.INVALID_GRANT,
                    "Unsupported subject client: " + clientId
            );
        };
    }

    private OAuth2AuthenticationException error(String errorCode, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode, description, null));
    }
}


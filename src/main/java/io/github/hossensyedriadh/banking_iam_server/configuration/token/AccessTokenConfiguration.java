package io.github.hossensyedriadh.banking_iam_server.configuration.token;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.ArrayList;
import java.util.Collections;

@Configuration
public class AccessTokenConfiguration {
    private static final String SWITCH_CLIENT_ID = "atm-pos-switch";
    private static final String GATEWAY_CLIENT_ID = "banking-api-gateway";
    private static final String GATEWAY_AUDIENCE = "banking-api-gateway";
    private static final String CBS_AUDIENCE = "cbs";

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> accessTokenCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            String clientId = context.getRegisteredClient().getClientId();
            AuthorizationGrantType grantType = context.getAuthorizationGrantType();

            if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(grantType)) {
                if (SWITCH_CLIENT_ID.equals(clientId) || GATEWAY_CLIENT_ID.equals(clientId)) {
                    context.getClaims().audience(
                            new ArrayList<>(Collections.singleton(GATEWAY_AUDIENCE))
                    );
                }

                return;
            }

            if (AuthorizationGrantType.TOKEN_EXCHANGE.equals(grantType)) {
                context.getClaims().audience(
                        new ArrayList<>(Collections.singleton(CBS_AUDIENCE))
                );
            }
        };
    }
}

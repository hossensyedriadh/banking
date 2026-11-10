package io.github.hossensyedriadh.banking_iam_server.configuration.token;

import io.github.hossensyedriadh.banking_iam_server.entity.IamUser;
import io.github.hossensyedriadh.banking_iam_server.entity.IamUserProfile;
import io.github.hossensyedriadh.banking_iam_server.repository.IamUserRepository;
import io.github.hossensyedriadh.banking_iam_server.util.OidcClaims;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Configuration
public class AccessTokenConfiguration {
    private final IamUserRepository userRepository;

    @Autowired
    public AccessTokenConfiguration(IamUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> accessTokenCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                final String SWITCH_CLIENT_ID = "atm-pos-switch";
                final String GATEWAY_CLIENT_ID = "banking-api-gateway";
                final String INTERNET_BANKING_CLIENT_ID = "internet-banking";

                final String GATEWAY_AUDIENCE = "banking-api-gateway";
                final String CBS_AUDIENCE = "cbs";

                String clientId = context.getRegisteredClient().getClientId();
                AuthorizationGrantType grantType = context.getAuthorizationGrantType();

                if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(grantType)) {
                    if (SWITCH_CLIENT_ID.equals(clientId) || GATEWAY_CLIENT_ID.equals(clientId)) {
                        context.getClaims().audience(new ArrayList<>(List.of(GATEWAY_AUDIENCE)));
                    }

                    return;
                }

                if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(grantType) && INTERNET_BANKING_CLIENT_ID.equals(clientId)) {
                    context.getClaims().audience(new ArrayList<>(
                            Arrays.asList(INTERNET_BANKING_CLIENT_ID,
                                    GATEWAY_AUDIENCE))
                    );
                }

                if (AuthorizationGrantType.TOKEN_EXCHANGE.equals(grantType)) {
                    context.getClaims().audience(new ArrayList<>(List.of(CBS_AUDIENCE)));
                }
            }


            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                Set<String> scopes = context.getAuthorizedScopes();

                boolean hasProfileScope = scopes.contains(OidcScopes.PROFILE);
                boolean hasEmailScope = scopes.contains(OidcScopes.EMAIL);
                boolean hasPhoneScope = scopes.contains(OidcScopes.PHONE);

                if (!hasProfileScope && !hasEmailScope && !hasPhoneScope) {
                    return;
                }

                Authentication principal = context.getPrincipal();

                if (principal == null || !principal.isAuthenticated() || principal instanceof AnonymousAuthenticationToken) {
                    return;
                }

                IamUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
                IamUserProfile profile = user.getProfile();

                if (profile == null) {
                    return;
                }

                JwtClaimsSet.Builder claims = context.getClaims();

                if (hasProfileScope) {
                    String fullName = Stream.of(profile.getFirstName(), profile.getMiddleName(), profile.getLastName()).filter(Objects::nonNull)
                            .map(String::trim).filter(name -> !name.isEmpty())
                            .collect(Collectors.joining(" "));

                    claims.claim(OidcClaims.FULL_NAME, fullName);
                }

                if (hasEmailScope) {
                    claims.claim(OidcClaims.EMAIL, profile.getEmail()).claim(OidcClaims.EMAIL_VERIFIED, profile.isEmailVerified());
                }

                if (hasPhoneScope) {
                    claims.claim(OidcClaims.PHONE_NO, profile.getPhoneNo()).claim(OidcClaims.PHONE_VERIFIED, profile.isPhoneNoVerified());
                }
            }
        };
    }
}

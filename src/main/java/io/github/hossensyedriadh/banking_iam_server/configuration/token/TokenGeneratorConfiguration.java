package io.github.hossensyedriadh.banking_iam_server.configuration.token;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.token.*;

@Configuration
public class TokenGeneratorConfiguration {
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder, OAuth2TokenCustomizer<JwtEncodingContext> accessTokenCustomizer) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(accessTokenCustomizer);

        return new DelegatingOAuth2TokenGenerator(jwtGenerator, new OAuth2AccessTokenGenerator(), new OAuth2RefreshTokenGenerator());
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
}

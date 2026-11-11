package io.github.hossensyedriadh.banking_iam_server.configuration.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.hossensyedriadh.banking_iam_server.configuration.properties.SigningKeyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(SigningKeyProperties.class)
public class SigningKeyConfiguration {
    @Bean
    public RSAKey signingRsaKey(SigningKeyProperties properties) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            try (InputStream input = Files.newInputStream(Path.of(properties.keystore()))) {
                keyStore.load(input, properties.storePassword().toCharArray());
            }

            Key key = keyStore.getKey(properties.alias(), properties.keyPassword().toCharArray());

            if (!(key instanceof RSAPrivateKey privateKey)) {
                throw new IllegalStateException("Configured alias does not contain RSA private key");
            }

            Certificate certificate = keyStore.getCertificate(properties.alias());

            if (certificate == null || !(certificate.getPublicKey() instanceof RSAPublicKey publicKey)) {
                throw new IllegalStateException("Configured alias does not contain RSA public key");
            }

            return new RSAKey.Builder(publicKey).privateKey(privateKey)
                    .keyID(properties.keyId()).build();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load signing key", e);
        }
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey signingRsaKey) {
        JWKSet jwkSet = new JWKSet(signingRsaKey);

        return (jwkSelector, _) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey signingRsaKey, AuthorizationServerSettings settings) throws Exception {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(signingRsaKey.toRSAPublicKey())
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(Objects.requireNonNull(settings.getIssuer())));

        return decoder;
    }
}

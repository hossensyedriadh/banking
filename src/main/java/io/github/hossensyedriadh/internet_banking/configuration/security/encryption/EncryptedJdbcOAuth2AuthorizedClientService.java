package io.github.hossensyedriadh.internet_banking.configuration.security.encryption;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EncryptedJdbcOAuth2AuthorizedClientService extends JdbcOAuth2AuthorizedClientService {
    private final TokenEncryptionService encryptionService;

    public EncryptedJdbcOAuth2AuthorizedClientService(JdbcOperations jdbcOperations, ClientRegistrationRepository clientRegistrationRepository,
                                                      TokenEncryptionService tokenEncryptionService) {
        super(jdbcOperations, clientRegistrationRepository);
        this.encryptionService = tokenEncryptionService;

        super.setAuthorizedClientParametersMapper(holder -> {
            List<SqlParameterValue> parameters = new OAuth2AuthorizedClientParametersMapper().apply(holder);

            OAuth2AuthorizedClient authorizedClient = holder.getAuthorizedClient();

            String registrationId = authorizedClient.getClientRegistration().getRegistrationId();

            String principalName = holder.getPrincipal().getName();

            SqlParameterValue accessToken = parameters.get(3); //as access_token_value column is the 4th column of the oauth2_authorized_client table

            parameters.set(3, new SqlParameterValue(Types.BLOB, this.encryptionService.encrypt(authorizedClient.getAccessToken().getTokenValue(),
                    context(registrationId, principalName, "access"))));

            SqlParameterValue refreshToken = parameters.get(7); //as refresh_token_value column is the 8th column of the oauth2_authorized_client table

            if (refreshToken.getValue() != null) {
                parameters.set(7, new SqlParameterValue(Types.BLOB, this.encryptionService.encrypt(Objects.requireNonNull(authorizedClient.getRefreshToken()).getTokenValue(),
                        context(registrationId, principalName, "refresh"))));
            }

            return parameters;
        });

        super.setAuthorizedClientRowMapper(new EncryptedAuthorizedClientRowMapper(clientRegistrationRepository, this.encryptionService));
    }

    private static String context(String registrationId, String principalName, String tokenType) {
        return registrationId + ":" + principalName + ":" + tokenType;
    }

    private record EncryptedAuthorizedClientRowMapper(ClientRegistrationRepository clientRegistrationRepository,
                                                      TokenEncryptionService encryptionService)
            implements RowMapper<OAuth2AuthorizedClient> {
        @Override
        public OAuth2AuthorizedClient mapRow(ResultSet rs, int rowNum) throws SQLException {
            String registrationId = rs.getString("client_registration_id");
            String principalName = rs.getString("principal_name");

            ClientRegistration registration = this.clientRegistrationRepository.findByRegistrationId(registrationId);

            if (registration == null) {
                throw new IllegalStateException("Client registration not found: " + registrationId);
            }

            OAuth2AccessToken.TokenType tokenType = OAuth2AccessToken.TokenType.BEARER;

            String storedTokenType = rs.getString("access_token_type");

            if (storedTokenType != null && !OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(storedTokenType)) {
                throw new IllegalStateException("Unsupported access token type: " + storedTokenType);
            }

            String accessTokenValue = this.encryptionService.decrypt(rs.getBytes("access_token_value"), context(registrationId, principalName, "access"));
            Timestamp accessIssuedAt = rs.getTimestamp("access_token_issued_at");
            Timestamp accessExpiresAt = rs.getTimestamp("access_token_expires_at");

            Instant issuedAt = accessIssuedAt != null ? accessIssuedAt.toInstant() : null;
            Instant expiresAt = accessExpiresAt != null ? accessExpiresAt.toInstant() : null;

            Set<String> scopes = Collections.emptySet();

            String accessTokenScopes = rs.getString("access_token_scopes");

            if (accessTokenScopes != null) {
                scopes = StringUtils.commaDelimitedListToSet(accessTokenScopes);
            }

            OAuth2AccessToken accessToken = new OAuth2AccessToken(tokenType, accessTokenValue, issuedAt, expiresAt, scopes);
            OAuth2RefreshToken refreshToken = null;

            byte[] refreshTokenBytes = rs.getBytes("refresh_token_value");

            if (refreshTokenBytes != null) {
                String refreshTokenValue = this.encryptionService.decrypt(refreshTokenBytes, context(registrationId, principalName, "refresh"));
                Timestamp refreshIssuedAt = rs.getTimestamp("refresh_token_issued_at");
                refreshToken = new OAuth2RefreshToken(refreshTokenValue, refreshIssuedAt != null ? refreshIssuedAt.toInstant() : null);
            }

            return new OAuth2AuthorizedClient(registration, principalName, accessToken, refreshToken);
        }
    }
}

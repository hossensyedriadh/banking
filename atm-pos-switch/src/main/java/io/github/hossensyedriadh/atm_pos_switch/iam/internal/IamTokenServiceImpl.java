package io.github.hossensyedriadh.atm_pos_switch.iam.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.atm_pos_switch.iam.api.service.IamTokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
class IamTokenServiceImpl implements IamTokenService {
    private final RestClient restClient;

    @Value("${iam.token-uri}")
    private String tokenUri;

    @Value("${iam.client-id}")
    private String clientId;

    @Value("${iam.client-secret}")
    private String clientSecret;

    @Value("${iam.scopes}")
    private List<String> scopes;

    private volatile CachedToken cachedToken;

    @Autowired
    public IamTokenServiceImpl(@Qualifier("iamRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public String getAccessToken() {
        CachedToken current = this.cachedToken;

        if (current != null && !current.isExpiringSoon()) {
            return current.value();
        }

        synchronized (this) {
            current = this.cachedToken;

            if (current != null && !current.isExpiringSoon()) {
                return current.value();
            }

            this.cachedToken = requestToken();

            return this.cachedToken.value();
        }
    }

    private CachedToken requestToken() {
        String credentials = this.clientId + ":" + this.clientSecret;

        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        try {
            TokenResponse response = this.restClient.post().uri(this.tokenUri)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(new LinkedMultiValueMap<>(Map.of(
                            "grant_type", List.of("client_credentials"),
                            "scope", List.of(String.join(" ", this.scopes))
                    )))
                    .retrieve()
                    .body(TokenResponse.class);

            if (response == null || response.accessToken() == null || response.accessToken().isBlank() || response.expiresIn() <= 0) {
                log.error("IAM returned an invalid token response: {}", response);
                throw new IllegalStateException("IAM returned an invalid token response");
            }

            return CachedToken.fromExpiresIn(response.accessToken(), response.expiresIn());
        } catch (RestClientResponseException exception) {
            log.error("IAM token request failed with HTTP {}", exception.getStatusCode().value());
            throw new IllegalStateException("IAM token request failed with HTTP " + exception.getStatusCode().value());
        } catch (RestClientException exception) {
            log.error("Unable to obtain access token from IAM", exception);
            throw new IllegalStateException("Unable to obtain access token from IAM", exception);
        }
    }

    private record CachedToken(String value, long expiresAtMillis) {
        private static CachedToken fromExpiresIn(String value, long expiresInSeconds) {
            long now = System.currentTimeMillis();
            long safetyMarginSeconds = Math.min(30L, expiresInSeconds / 10L);
            long expiresAtMillis = now + (expiresInSeconds - safetyMarginSeconds) * 1_000L;

            return new CachedToken(value, expiresAtMillis);
        }

        private boolean isExpiringSoon() {
            return System.currentTimeMillis() >= expiresAtMillis;
        }
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("expires_in") long expiresIn) {
    }
}

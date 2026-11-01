package io.github.hossensyedriadh.banking_api_gateway.security.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuth2TokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        long expiresIn,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("issued_token_type")
        String issuedTokenType) {
}

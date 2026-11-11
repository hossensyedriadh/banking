package io.github.hossensyedriadh.banking_api_gateway.tokenexchange;

import java.time.Instant;
import java.util.Set;

public record DelegatedToken(String accessToken, Instant expiresAt, Set<String> scopes) {
}

package io.github.hossensyedriadh.banking_iam_server.configuration.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public final class PersistentClientPrincipal extends AbstractAuthenticationToken {
    private final String clientId;

    @JsonCreator
    public PersistentClientPrincipal(@JsonProperty("clientId") String clientId) {
        super(List.of());
        this.clientId = clientId;
        super.setAuthenticated(true);
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @Override
    public Object getPrincipal() {
        return clientId;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    @NonNull
    public String getName() {
        return this.clientId;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        super.setAuthenticated(authenticated);
    }
}
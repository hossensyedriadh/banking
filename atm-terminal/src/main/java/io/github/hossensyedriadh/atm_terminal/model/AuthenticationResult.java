package io.github.hossensyedriadh.atm_terminal.model;

import io.github.hossensyedriadh.atm_terminal.enumerator.AuthenticationStatus;

public record AuthenticationResult(AuthenticationStatus status, String sessionToken) {
    public static AuthenticationResult success(String sessionToken) {
        return new AuthenticationResult(AuthenticationStatus.SUCCESS, sessionToken);
    }

    public static AuthenticationResult failure(AuthenticationStatus status) {
        if (status == AuthenticationStatus.SUCCESS) {
            throw new IllegalArgumentException("Invalid authentication status");
        }

        return new AuthenticationResult(status, null);
    }

    public boolean successful() {
        return status == AuthenticationStatus.SUCCESS;
    }
}

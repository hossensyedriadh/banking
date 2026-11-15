package io.github.hossensyedriadh.cbs.authorization.internal.impl;

import io.github.hossensyedriadh.cbs.authorization.api.service.CurrentActorService;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
class CurrentActorServiceImpl implements CurrentActorService {
    @Override
    @NonNull
    public String getSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
            throw new ApiResourceException("Authenticated JWT required", HttpStatus.UNAUTHORIZED);
        }

        String subject = jwtAuthentication.getToken().getSubject();

        if (subject == null || subject.isBlank()) {
            throw new ApiResourceException("JWT subject is missing", HttpStatus.UNAUTHORIZED);
        }

        return subject;
    }
}

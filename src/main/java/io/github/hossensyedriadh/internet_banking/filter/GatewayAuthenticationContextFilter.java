package io.github.hossensyedriadh.internet_banking.filter;

import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationContext;
import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewayAuthenticationContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        GatewayAuthenticationContext.set(GatewayAuthenticationType.CUSTOMER);

        try {
            filterChain.doFilter(request, response);
        } finally {
            GatewayAuthenticationContext.clear();
        }
    }
}

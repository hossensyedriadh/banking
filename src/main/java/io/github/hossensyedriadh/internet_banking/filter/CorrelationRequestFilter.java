package io.github.hossensyedriadh.internet_banking.filter;

import io.github.hossensyedriadh.internet_banking.util.CustomAttributes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class CorrelationRequestFilter extends OncePerRequestFilter {
    private static final String CORRELATION_ID_CONTEXT_KEY = "correlationId";
    private static final String REQUEST_ID_CONTEXT_KEY = "requestId";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Map<String, String> previousContext = ThreadContext.getImmutableContext();

        String correlationId = this.getOrGenerateCorrelationId(request);
        String requestId = request.getHeader(CustomAttributes.REQUEST_ID);

        try {
            ThreadContext.put(CORRELATION_ID_CONTEXT_KEY, correlationId);
            ThreadContext.put(REQUEST_ID_CONTEXT_KEY, requestId);

            response.setHeader(CustomAttributes.CORRELATION_ID, correlationId);
            response.setHeader(CustomAttributes.REQUEST_ID, requestId);

            filterChain.doFilter(request, response);
        } finally {
            ThreadContext.clearMap();
            ThreadContext.putAll(previousContext);
        }
    }

    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CustomAttributes.CORRELATION_ID);

        if (correlationId == null || !Pattern.matches("[a-zA-Z0-9._-]{1,100}", correlationId)) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }
}

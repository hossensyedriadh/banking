package io.github.hossensyedriadh.banking_api_gateway.filter;

import io.github.hossensyedriadh.banking_api_gateway.util.ClientIpAddressResolver;
import io.github.hossensyedriadh.banking_api_gateway.util.CustomHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class GatewayRequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LogManager.getLogger(GatewayRequestLoggingFilter.class);

    private final ClientIpAddressResolver clientIpAddressResolver;

    @Autowired
    public GatewayRequestLoggingFilter(ClientIpAddressResolver clientIpAddressResolver) {
        this.clientIpAddressResolver = clientIpAddressResolver;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        long startNanos = System.nanoTime();

        ServerWebExchange mutatedExchange = exchange.mutate().request(request -> request.headers(headers ->
                headers.set(CustomHeaders.REQUEST_ID, requestId))).build();

        mutatedExchange.getResponse().getHeaders().set(CustomHeaders.REQUEST_ID, requestId);

        ServerHttpRequest request = mutatedExchange.getRequest();

        String clientIpAddress = clientIpAddressResolver.resolve(request);
        String method = request.getMethod().name();
        String path = request.getPath().value();

        return chain.filter(mutatedExchange).doOnError(ex -> {
            logger.warn("Gateway request failed: requestId={}, clientIp={}, method={}, path={}, errorType={}", requestId, clientIpAddress, method, path, ex.getClass().getSimpleName());
        }).doFinally(signalType -> {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

            Route route = mutatedExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = route != null ? route.getId() : "unmatched";

            HttpStatusCode status = mutatedExchange.getResponse().getStatusCode();

            logger.info("Gateway request completed: requestId={}, clientIp={}, method={}, path={}, routeId={}, status={}, durationMs={}, signal={}",
                    requestId, clientIpAddress, method, path, routeId, status != null ? status.value() : "N/A", durationMs, signalType);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

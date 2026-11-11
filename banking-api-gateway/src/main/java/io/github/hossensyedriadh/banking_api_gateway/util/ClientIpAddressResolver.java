package io.github.hossensyedriadh.banking_api_gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientIpAddressResolver {
    public String resolve(ServerHttpRequest request) {
        if (request.getRemoteAddress() == null || request.getRemoteAddress().getAddress() == null) {
            return "unknown";
        }

        return request.getRemoteAddress().getAddress().getHostAddress();
    }
}

package io.github.hossensyedriadh.cbs.util;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.Serializable;

public class HttpUtil {
    private final HttpServletRequest request;
    private final Parser parser;

    public HttpUtil(HttpServletRequest request) {
        this.request = request;
        this.parser = new Parser();
    }

    public String parseClientAddress() {
        String[] headers = {
                "X-FORWARDED-FOR",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String value = this.request.getHeader(header);

            if (value == null || value.isEmpty()) {
                continue;
            }

            String[] parts = value.split(",");
            return parts[0];
        }

        return this.request.getRemoteAddr();
    }

    public UserAgentInfo parseUserAgent() {
        String ua = this.request.getHeader("User-Agent");

        if (ua == null || ua.isBlank()) {
            return null;
        }

        Client client = this.parser.parse(ua);

        return new UserAgentInfo(client.userAgent.family, client.userAgent.major,
                client.os.family, client.os.major, client.device.family);
    }

    public record UserAgentInfo(String browser, String browserVersion,
                                String os, String osVersion, String device) implements Serializable {
        @Override
        @NonNull
        public String toString() {
            return String.format("browser:%s; browser-version: %s; os: %s; os-version: %s; device: %s", browser, browserVersion, os, osVersion, device);
        }
    }
}

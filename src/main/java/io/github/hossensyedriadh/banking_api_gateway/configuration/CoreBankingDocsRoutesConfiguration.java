package io.github.hossensyedriadh.banking_api_gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//todo: temporary route for development
@Configuration
public class CoreBankingDocsRoutesConfiguration {
    @Bean
    public RouteLocator cbsDocsRoutes(RouteLocatorBuilder builder, @Value("${banking.cbs.base-url}") String cbsBaseUrl) {
        return builder.routes()
                .route("cbs-openapi-docs", route -> route
                        .path(
                                "/api/docs/cbs/v3/api-docs",
                                "/api/docs/cbs/v3/api-docs/**"
                        )
                        .filters(filters -> filters.rewritePath(
                                "/api/docs/cbs/(?<segment>.*)",
                                "/api/${segment}"
                        ))
                        .uri(cbsBaseUrl)
                )
                .route("cbs-swagger-ui", route -> route
                        .path("/api/docs/cbs/swagger-ui/**")
                        .filters(filters -> filters.rewritePath(
                                "/api/docs/cbs/(?<segment>.*)",
                                "/api/${segment}"
                        ))
                        .uri(cbsBaseUrl)
                )
                .route("cbs-swagger-ui-entry", route -> route
                        .path("/api/docs/cbs")
                        .filters(filters -> filters.rewritePath(
                                "/api/docs/cbs",
                                "/api/swagger-ui.html"
                        ))
                        .uri(cbsBaseUrl)
                )
                .route("cbs-swagger-ui-redirect", route -> route
                        .path("/api/swagger-ui/**")
                        .uri(cbsBaseUrl)
                )
                .route("cbs-openapi-redirect", route -> route
                        .path("/api/v3/api-docs", "/api/v3/api-docs/**")
                        .uri(cbsBaseUrl)
                )
                .build();
    }
}

package io.github.hossensyedriadh.banking_api_gateway.configuration.route;

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
                        .path("/cbs/docs/v3/api-docs", "/cbs/docs/v3/api-docs/**")
                        .filters(filters -> filters.rewritePath(
                                "/cbs/docs/(?<segment>.*)",
                                "/api/docs/cbs/${segment}"
                        )).uri(cbsBaseUrl)
                )
                .route("cbs-swagger-ui", route -> route
                        .path("/cbs/docs/swagger-ui/**")
                        .filters(filters -> filters.rewritePath(
                                "/cbs/docs/(?<segment>.*)",
                                "/api/${segment}"
                        )).uri(cbsBaseUrl)
                ).build();
    }
}

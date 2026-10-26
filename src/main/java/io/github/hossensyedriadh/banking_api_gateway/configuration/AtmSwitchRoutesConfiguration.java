package io.github.hossensyedriadh.banking_api_gateway.configuration;

import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.AtmEndpoints;
import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.CbsEndpoints;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class AtmSwitchRoutesConfiguration {
    @Bean
    public RouteLocator atmSwitchRoutes(RouteLocatorBuilder builder, @Value("${banking.cbs.base-url}") String cbsBaseUrl) {
        return builder.routes()
                .route("atm-account-balance", r -> r.path(AtmEndpoints.ACCOUNT_BALANCE)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                AtmEndpoints.ACCOUNT_BALANCE_REWRITE,
                                CbsEndpoints.ACCOUNT_BALANCE_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-account-info", r -> r.path(AtmEndpoints.ACCOUNT_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                AtmEndpoints.ACCOUNT_INFO_REWRITE,
                                CbsEndpoints.ACCOUNT_INFO_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-account-product-info", r -> r.path(AtmEndpoints.ACCOUNT_PRODUCT_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                AtmEndpoints.ACCOUNT_PRODUCT_INFO_REWRITE,
                                CbsEndpoints.ACCOUNT_PRODUCT_INFO_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-currency-info", r -> r.path(AtmEndpoints.CURRENCY_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                AtmEndpoints.CURRENCY_INFO_REWRITE,
                                CbsEndpoints.CURRENCY_INFO_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-card-info-by-pan", r -> r.path(AtmEndpoints.CARD_INFO_BY_PAN)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                AtmEndpoints.CARD_INFO_BY_PAN_REWRITE,
                                CbsEndpoints.CARD_INFO_BY_PAN_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-card-pin-verification", r -> r.path(AtmEndpoints.CARD_PIN_VERIFICATION)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                AtmEndpoints.CARD_PIN_VERIFICATION_REWRITE,
                                CbsEndpoints.CARD_PIN_VERIFICATION_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-transactions-search", r -> r.path(AtmEndpoints.TRANSACTIONS_SEARCH)
                        .and().method(HttpMethod.GET).and().query("from").and().query("to")
                        .filters(f -> f.rewritePath(
                                AtmEndpoints.TRANSACTIONS_SEARCH_REWRITE,
                                CbsEndpoints.TRANSACTIONS_SEARCH_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-transactions-by-idempotency", r -> r.path(AtmEndpoints.TRANSACTION_BY_IDEMPOTENCY)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                AtmEndpoints.TRANSACTION_BY_IDEMPOTENCY_REWRITE,
                                CbsEndpoints.TRANSACTION_BY_IDEMPOTENCY_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-withdrawal", r -> r.path(AtmEndpoints.TRANSACTION_WITHDRAWAL)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                AtmEndpoints.TRANSACTION_WITHDRAWAL_REWRITE,
                                CbsEndpoints.TRANSACTION_WITHDRAWAL_REWRITE))
                        .uri(cbsBaseUrl))
                .route("atm-reversal", r -> r.path(AtmEndpoints.TRANSACTION_REVERSAL)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                AtmEndpoints.TRANSACTION_REVERSAL_REWRITE,
                                CbsEndpoints.TRANSACTION_REVERSAL_REWRITE))
                        .uri(cbsBaseUrl))
                .build();
    }
}

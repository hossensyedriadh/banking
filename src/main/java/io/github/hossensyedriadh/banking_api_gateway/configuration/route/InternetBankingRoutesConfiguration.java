package io.github.hossensyedriadh.banking_api_gateway.configuration.route;

import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.CbsEndpoints;
import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.InternetBankingEndpoints;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class InternetBankingRoutesConfiguration {
    @Bean
    public RouteLocator internetBankingRoutes(RouteLocatorBuilder builder, @Value("${banking.cbs.base-url}") String cbsBaseUrl) {
        return builder.routes()
                .route("internet-banking-account-balance", r -> r.path(InternetBankingEndpoints.ACCOUNT_BALANCE)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.ACCOUNT_BALANCE_REWRITE,
                                CbsEndpoints.ACCOUNT_BALANCE_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-account-info", r -> r.path(InternetBankingEndpoints.ACCOUNT_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.ACCOUNT_INFO_REWRITE,
                                CbsEndpoints.ACCOUNT_INFO_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-account-product-info", r -> r.path(InternetBankingEndpoints.ACCOUNT_PRODUCT_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.ACCOUNT_PRODUCT_INFO_REWRITE,
                                CbsEndpoints.ACCOUNT_PRODUCT_INFO_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-currency-info", r -> r.path(InternetBankingEndpoints.CURRENCY_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.CURRENCY_INFO_REWRITE,
                                CbsEndpoints.CURRENCY_INFO_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-card-info-by-pan", r -> r.path(InternetBankingEndpoints.CARD_INFO_BY_PAN)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.CARD_INFO_BY_PAN_REWRITE,
                                CbsEndpoints.CARD_INFO_BY_PAN_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-card-info-by-id", r -> r.path(InternetBankingEndpoints.CARD_INFO_BY_ID)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.CARD_INFO_BY_ID_REWRITE,
                                CbsEndpoints.CARD_INFO_BY_ID_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-card-pin-verification", r -> r.path(InternetBankingEndpoints.CARD_PIN_VERIFICATION)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.CARD_PIN_VERIFICATION_REWRITE,
                                CbsEndpoints.CARD_PIN_VERIFICATION_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-transactions-search", r -> r.path(InternetBankingEndpoints.TRANSACTIONS_SEARCH)
                        .and().method(HttpMethod.GET).and().query("from").and().query("to")
                        .filters(f -> f.rewritePath(
                                InternetBankingEndpoints.TRANSACTIONS_SEARCH_REWRITE,
                                CbsEndpoints.TRANSACTIONS_SEARCH_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-transactions-by-idempotency", r -> r.path(InternetBankingEndpoints.TRANSACTION_BY_IDEMPOTENCY)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.TRANSACTION_BY_IDEMPOTENCY_REWRITE,
                                CbsEndpoints.TRANSACTION_BY_IDEMPOTENCY_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-withdrawal", r -> r.path(InternetBankingEndpoints.TRANSACTION_WITHDRAWAL)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.TRANSACTION_WITHDRAWAL_REWRITE,
                                CbsEndpoints.TRANSACTION_WITHDRAWAL_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-fund-transfer", r -> r.path(InternetBankingEndpoints.TRANSACTION_TRANSFER)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.TRANSACTION_TRANSFER_REWRITE,
                                CbsEndpoints.TRANSACTION_TRANSFER_REWRITE))
                        .uri(cbsBaseUrl))
                .route("internet-banking-reversal", r -> r.path(InternetBankingEndpoints.TRANSACTION_REVERSAL)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                InternetBankingEndpoints.TRANSACTION_REVERSAL_REWRITE,
                                CbsEndpoints.TRANSACTION_REVERSAL_REWRITE))
                        .uri(cbsBaseUrl))
                .build();
    }
}

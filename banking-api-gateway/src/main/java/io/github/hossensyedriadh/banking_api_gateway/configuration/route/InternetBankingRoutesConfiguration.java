package io.github.hossensyedriadh.banking_api_gateway.configuration.route;

import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.CbsEndpoints;
import io.github.hossensyedriadh.banking_api_gateway.endpoint.v1.InternetBankingEndpoints;
import io.github.hossensyedriadh.banking_api_gateway.enumerator.ChannelType;
import io.github.hossensyedriadh.banking_api_gateway.tokenexchange.DelegatedTokenGatewayFilterFactory;
import io.github.hossensyedriadh.banking_api_gateway.util.InternetBankingOAuth2Scopes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class InternetBankingRoutesConfiguration {
    private final DelegatedTokenGatewayFilterFactory delegatedTokenGatewayFilterFactory;

    @Autowired
    public InternetBankingRoutesConfiguration(DelegatedTokenGatewayFilterFactory delegatedTokenGatewayFilterFactory) {
        this.delegatedTokenGatewayFilterFactory = delegatedTokenGatewayFilterFactory;
    }

    @Bean
    public RouteLocator internetBankingRoutes(RouteLocatorBuilder builder, @Value("${banking.cbs.base-url}") String cbsBaseUrl) {
        return builder.routes()
                .route("internet-banking-account-balance", r -> r.path(InternetBankingEndpoints.ACCOUNT_BALANCE)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.ACCOUNT_BALANCE_REWRITE,
                                        CbsEndpoints.ACCOUNT_BALANCE_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.BALANCE_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-account-list", r -> r.path(InternetBankingEndpoints.ACCOUNT_LIST)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.ACCOUNT_LIST_REWRITE,
                                        CbsEndpoints.ACCOUNT_LIST_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.ACCOUNT_INFO_LIST))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-account-info", r -> r.path(InternetBankingEndpoints.ACCOUNT_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.ACCOUNT_INFO_REWRITE,
                                        CbsEndpoints.PRINCIPAL_ACCOUNT_INFO_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.ACCOUNT_INFO_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-account-info-account-number", r -> r.path(InternetBankingEndpoints.ACCOUNT_INFO_BY_ACCOUNT_NUMBER)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.ACCOUNT_INFO_BY_ACCOUNT_NUMBER_REWRITE,
                                        CbsEndpoints.ACCOUNT_INFO_BY_ACCOUNT_NUMBER_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.ACCOUNT_INFO_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-account-product-info", r -> r.path(InternetBankingEndpoints.ACCOUNT_PRODUCT_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.ACCOUNT_PRODUCT_INFO_REWRITE,
                                        CbsEndpoints.ACCOUNT_PRODUCT_INFO_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.ACCOUNT_PRODUCT_INFO_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-currency-info", r -> r.path(InternetBankingEndpoints.CURRENCY_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.CURRENCY_INFO_REWRITE,
                                        CbsEndpoints.CURRENCY_INFO_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.CURRENCY_INFO_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-branch-info", r -> r.path(InternetBankingEndpoints.BRANCH_INFO)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.BRANCH_INFO_REWRITE,
                                        CbsEndpoints.BRANCH_INFO_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.BRANCH_INFO_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-card-info-by-pan", r -> r.path(InternetBankingEndpoints.CARD_INFO_BY_PAN)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.CARD_INFO_BY_PAN_REWRITE,
                                        CbsEndpoints.CARD_INFO_BY_PAN_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.CARD_INFO_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-card-info-by-id", r -> r.path(InternetBankingEndpoints.CARD_INFO_BY_ID)
                        .and().method(HttpMethod.GET).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.CARD_INFO_BY_ID_REWRITE,
                                        CbsEndpoints.CARD_INFO_BY_ID_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.CARD_INFO_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-card-pin-verification", r -> r.path(InternetBankingEndpoints.CARD_PIN_VERIFICATION)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.CARD_PIN_VERIFICATION_REWRITE,
                                        CbsEndpoints.CARD_PIN_VERIFICATION_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.CARD_PIN_VERIFY))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-transactions-search", r -> r.path(InternetBankingEndpoints.TRANSACTIONS_SEARCH)
                        .and().method(HttpMethod.GET).and().query("from").and().query("to").and().query("accountId")
                        .filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.TRANSACTIONS_SEARCH_REWRITE,
                                        CbsEndpoints.TRANSACTIONS_SEARCH_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.TRANSACTIONS_SEARCH_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-transaction-by-id", r -> r.path(InternetBankingEndpoints.TRANSACTION_BY_ID)
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.TRANSACTION_BY_ID_REWRITE,
                                        CbsEndpoints.TRANSACTION_BY_ID_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.TRANSACTION_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-transaction-by-idempotency", r -> r.path(InternetBankingEndpoints.TRANSACTION_BY_IDEMPOTENCY)
                        .and().method(HttpMethod.GET).and().query("accountId").filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.TRANSACTION_BY_IDEMPOTENCY_REWRITE,
                                        CbsEndpoints.TRANSACTION_BY_IDEMPOTENCY_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.TRANSACTION_READ))))
                        .uri(cbsBaseUrl))
                .route("internet-banking-fund-transfer", r -> r.path(InternetBankingEndpoints.TRANSACTION_TRANSFER)
                        .and().method(HttpMethod.POST).filters(f -> f.rewritePath(
                                        InternetBankingEndpoints.TRANSACTION_TRANSFER_REWRITE,
                                        CbsEndpoints.TRANSACTION_TRANSFER_REWRITE)
                                .filter(this.delegatedTokenGatewayFilterFactory
                                        .apply(new DelegatedTokenGatewayFilterFactory.Config(ChannelType.INTERNET_BANKING, InternetBankingOAuth2Scopes.TRANSFER_CREATE))))
                        .uri(cbsBaseUrl))
                .build();
    }
}

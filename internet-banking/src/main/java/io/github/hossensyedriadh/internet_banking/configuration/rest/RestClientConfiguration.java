package io.github.hossensyedriadh.internet_banking.configuration.rest;

import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationContext;
import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationType;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingException;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Log4j2
@Configuration
public class RestClientConfiguration {
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final OAuth2AuthorizedClientManager backgroundAuthorizedClientManager;

    @Autowired
    public RestClientConfiguration(@Qualifier("oAuth2AuthorizedClientManager") OAuth2AuthorizedClientManager authorizedClientManager,
                                   @Qualifier("backgroundAuthorizedClientManager") OAuth2AuthorizedClientManager backgroundAuthorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
        this.backgroundAuthorizedClientManager = backgroundAuthorizedClientManager;
    }

    @Value("${spring.security.oauth2.client.registration.internet-banking.client-id}")
    private String iBankingClientId;

    @Value("${spring.security.oauth2.client.registration.internet-banking-service.client-id}")
    private String iBankingServiceClientId;

    @Value("${core-banking.api-gateway.base-url}")
    private String coreBankingApiGatewayBaseUrl;

    private static final String INTERNET_BANKING_BASE_URL = "/api/v1/internet-banking";

    @Bean("gatewayHttpClient")
    public CloseableHttpClient gatewayHttpClient() {
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(50).setMaxConnPerRoute(20).setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(5)).build())
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(10))
                .setResponseTimeout(Timeout.ofSeconds(15))
                .build();

        return HttpClients.custom().setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(45))
                .disableAutomaticRetries()
                .build();
    }

    @Bean("gatewayRestClient")
    public RestClient gatewayRestClient(CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder().baseUrl(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL).requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    GatewayAuthenticationType type = GatewayAuthenticationContext.get();

                    if (type == null) {
                        throw new IllegalStateException("Gateway authentication context is missing");
                    }

                    String accessToken = switch (type) {
                        case CUSTOMER -> this.getAccessToken();
                        case SERVICE -> this.getBackgroundAccessToken();
                    };

                    request.getHeaders().setBearerAuth(accessToken);

                    return execution.execute(request, body);
                })
                .build();
    }

    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Customer not authenticated");
            throw new IllegalStateException("No authenticated customer");
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(this.iBankingClientId)
                .principal(authentication).build();

        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null) {
            log.error("Unable to authorize Internet Banking OAuth2 client [ID: {}]", this.iBankingClientId);
            throw new CoreBankingException("Unable to authorize Internet Banking OAuth2 client");
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }

    private String getBackgroundAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(this.iBankingServiceClientId)
                .principal("internet-banking-service")
                .build();

        OAuth2AuthorizedClient authorizedClient = this.backgroundAuthorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null) {
            log.error("Unable to authorize Internet Banking Service OAuth2 client [ID: {}]", this.iBankingClientId);
            throw new CoreBankingException("Unable to authorize Internet Banking service");
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }
}

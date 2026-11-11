package io.github.hossensyedriadh.atm_pos_switch.configuration.http;

import io.github.hossensyedriadh.atm_pos_switch.interceptor.IamAuthenticationInterceptor;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class CoreBankingHttpClientConfiguration {
    private final IamAuthenticationInterceptor iamAuthenticationInterceptor;

    @Autowired
    public CoreBankingHttpClientConfiguration(IamAuthenticationInterceptor iamAuthenticationInterceptor) {
        this.iamAuthenticationInterceptor = iamAuthenticationInterceptor;
    }

    @Bean(name = "cbsHttpClient", destroyMethod = "close")
    public CloseableHttpClient cbsHttpClient() {
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

    @Bean("cbsRestClient")
    public RestClient cbsRestClient(@Qualifier("cbsHttpClient") CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder().requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor(this.iamAuthenticationInterceptor)
                .build();
    }
}

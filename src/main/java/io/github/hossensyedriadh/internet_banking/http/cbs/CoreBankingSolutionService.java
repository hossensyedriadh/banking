package io.github.hossensyedriadh.internet_banking.http.cbs;

import io.github.hossensyedriadh.internet_banking.customer.model.PrincipalIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.service.CustomerIdentityService;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingException;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.*;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class CoreBankingSolutionService {
    private final RestClient restClient;
    private final CustomerIdentityService customerIdentityService;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${core-banking.api-gateway.base-url}")
    private String coreBankingApiGatewayBaseUrl;

    @Value("${iam.client-id}")
    private String iamClientId;

    private static final String INTERNET_BANKING_BASE_URL = "/api/v1/internet-banking";

    @Autowired
    public CoreBankingSolutionService(RestClient restClient, CustomerIdentityService customerIdentityService,
                                      OAuth2AuthorizedClientManager authorizedClientManager) {
        this.restClient = restClient;
        this.customerIdentityService = customerIdentityService;
        this.authorizedClientManager = authorizedClientManager;
    }

    public List<CbsAccountResponse> fetchBankAccounts() {
        PrincipalIdentityModel principalIdentity = this.customerIdentityService.getPrincipal();
        Long cbsCustomerId = principalIdentity.getCustomerIdentity().getCbsCustomerId();

        try {
            List<CbsAccountResponse> accounts = this.restClient.get().uri(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/accounts/customer/" + cbsCustomerId + "/list")
                    .headers(headers -> headers.setBearerAuth(this.getAccessToken()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (accounts == null) {
                log.error("CBS/Gateway returned empty response for accounts information fetch for Customer [CBS Customer ID: {}]", cbsCustomerId);
                throw new CoreBankingException("Failed to fetch account information");
            }

            return accounts;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch bank account mappings of CBS Customer [ID: {}]", e.getStatusCode().value(), cbsCustomerId, e);
            throw new CoreBankingCommunicationException("Failed to fetch bank accounts", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for bank account mappings of CBS Customer [ID: {}]", cbsCustomerId, e);
            throw new CoreBankingException("Failed to fetch bank accounts", e);
        }
    }

    public CbsAccountResponse fetchAccountInfo(@NonNull Long accountId) {
        try {
            CbsAccountResponse accountResponse = this.restClient.get().uri(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/accounts/" + accountId)
                    .headers(headers -> headers.setBearerAuth(this.getAccessToken()))
                    .retrieve()
                    .body(CbsAccountResponse.class);

            if (accountResponse == null) {
                log.error("CBS/Gateway returned empty response for Account [ID: {}] information fetch", accountId);
                throw new CoreBankingException("Failed to fetch account information");
            }

            return accountResponse;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch Account [ID: {}] information", e.getStatusCode().value(), accountId, e);
            throw new CoreBankingCommunicationException("Failed to fetch account information", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for Account [ID: {}] information fetch", accountId, e);
            throw new CoreBankingException("Failed to fetch account information", e);
        }
    }

    public CbsAccountBalanceResponse fetchAccountBalance(@NonNull Long accountId) {
        try {
            CbsAccountBalanceResponse balanceResponse = this.restClient.get().uri(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/accounts/" + accountId + "/balance")
                    .headers(headers -> headers.setBearerAuth(this.getAccessToken()))
                    .retrieve()
                    .body(CbsAccountBalanceResponse.class);

            if (balanceResponse == null) {
                log.error("CBS/Gateway returned empty response for Account [ID: {}] balance fetch", accountId);
                throw new CoreBankingException("Failed to fetch account balance");
            }

            return balanceResponse;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch Account Balance [Account ID: {}] information", e.getStatusCode().value(), accountId, e);
            throw new CoreBankingCommunicationException("Failed to fetch account balance", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for Account Balance [Account ID: {}] fetch", accountId, e);
            throw new CoreBankingException("Failed to fetch account balance", e);
        }
    }

    public CbsAccountProductResponse fetchAccountProductInfo(@NonNull Long productId) {
        try {
            CbsAccountProductResponse productResponse = this.restClient.get().uri(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/account-products/" + productId)
                    .headers(headers -> headers.setBearerAuth(this.getAccessToken()))
                    .retrieve()
                    .body(CbsAccountProductResponse.class);

            if (productResponse == null) {
                log.error("CBS/Gateway returned empty response for Account Product [ID: {}] information fetch", productId);
                throw new CoreBankingException("Failed to fetch account product information");
            }

            return productResponse;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch Account Product [ID: {}] information", e.getStatusCode().value(), productId, e);
            throw new CoreBankingCommunicationException("Failed to fetch account product information", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for Account Product [ID: {}] information fetch", productId, e);
            throw new CoreBankingException("Failed to fetch account product information", e);
        }
    }

    public CbsCurrencyResponse fetchCurrencyInfo(@NonNull Long currencyId) {
        try {
            CbsCurrencyResponse currencyResponse = this.restClient.get().uri(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/currencies/" + currencyId)
                    .headers(headers -> headers.setBearerAuth(this.getAccessToken()))
                    .retrieve()
                    .body(CbsCurrencyResponse.class);

            if (currencyResponse == null) {
                log.error("CBS/Gateway returned empty response for Currency [ID: {}] information fetch", currencyId);
                throw new CoreBankingException("Failed to fetch currency information");
            }

            return currencyResponse;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch Currency [ID: {}] information", e.getStatusCode().value(), currencyId, e);
            throw new CoreBankingCommunicationException("Failed to fetch currency information", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for Currency [ID: {}] information fetch", currencyId, e);
            throw new CoreBankingException("Failed to fetch currency information", e);
        }
    }

    public CbsBranchResponse fetchBranchInfo(@NonNull Long branchId) {
        try {
            String token = this.getAccessToken();

            CbsBranchResponse branchResponse = this.restClient.get().uri(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/branches/" + branchId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .body(CbsBranchResponse.class);

            if (branchResponse == null) {
                log.error("CBS/Gateway returned empty response for Branch [ID: {}] information fetch", branchId);
                throw new CoreBankingException("Failed to fetch branch information");
            }

            return branchResponse;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch Branch [ID: {}] information", e.getStatusCode().value(), branchId, e);
            throw new CoreBankingCommunicationException("Failed to fetch branch information", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for Branch [ID: {}] information fetch", branchId, e);
            throw new CoreBankingException("Failed to fetch branch information", e);
        }
    }

    public CbsTransactionPage fetchTransactions(CbsTransactionsSearch search) {
        URI uri = UriComponentsBuilder.fromUriString(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/transactions")
                .queryParam("from", search.from().toString())
                .queryParam("to", search.to().toString())
                .queryParam("page", search.page())
                .queryParam("size", search.size())
                .queryParam("accountId", search.accountId())
                .queryParamIfPresent("type", Optional.ofNullable(search.type()))
                .queryParamIfPresent("status", Optional.ofNullable(search.status()))
                .build().encode()
                .toUri();

        try {
            String token = this.getAccessToken();

            CbsTransactionPage response = this.restClient.get().uri(uri).headers(headers -> headers.setBearerAuth(token))
                    .retrieve().body(CbsTransactionPage.class);

            if (response == null || response.getContent() == null) {
                log.error("CBS/Gateway returned an empty transactions response");
                throw new CoreBankingException("CBS/Gateway returned an empty transactions response");
            }

            return response;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch transactions", e.getStatusCode().value(), e);
            throw new CoreBankingCommunicationException("Failed to fetch transactions", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for transactions fetch", e);
            throw new CoreBankingException("Failed to fetch transactions", e);
        }
    }

    public CbsTransactionResponse fetchTransaction(Long id) {
        try {
            String token = this.getAccessToken();

            CbsTransactionResponse response = this.restClient.get().uri(this.coreBankingApiGatewayBaseUrl + INTERNET_BANKING_BASE_URL + "/transactions/" + id)
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve().body(CbsTransactionResponse.class);

            if (response == null) {
                log.error("CBS/Gateway returned an empty transaction response");
                throw new CoreBankingException("CBS/Gateway returned an empty transaction response");
            }

            return response;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} while trying to fetch transaction [ID: {}]", e.getStatusCode().value(), id, e);
            throw new CoreBankingCommunicationException("Failed to fetch transaction", HttpStatus.resolve(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.error("CBS/Gateway returned invalid response for transaction [ID: {}] fetch", id, e);
            throw new CoreBankingException("Failed to fetch transaction", e);
        }
    }

    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Customer not authenticated");
            throw new IllegalStateException("No authenticated customer");
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(this.iamClientId)
                .principal(authentication).build();

        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null) {
            log.error("Unable to authorize Internet Banking OAuth2 client [ID: {}]", this.iamClientId);
            throw new CoreBankingException("Unable to authorize Internet Banking OAuth2 client");
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }
}

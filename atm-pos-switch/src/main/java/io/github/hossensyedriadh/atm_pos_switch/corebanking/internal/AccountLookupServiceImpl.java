package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.AccountLookupService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Log4j2
@Service
public class AccountLookupServiceImpl implements AccountLookupService {
    private final RestClient restClient;

    @Value("${core-banking.atm-gateway.base-url}")
    private String coreBankingAtmGatewayBaseUrl;

    @Autowired
    public AccountLookupServiceImpl(@Qualifier("cbsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CoreBankingAccountResponse lookupAccount(String accountNumber) {
        try {
            CoreBankingAccountResponse accountResponse = this.restClient.get().uri(this.coreBankingAtmGatewayBaseUrl + "/accounts/account-number/" + accountNumber)
                    .retrieve().body(CoreBankingAccountResponse.class);

            if (accountResponse == null) {
                log.error("CBS/Gateway returned an empty response for account lookup");
                throw new CoreBankingException("CBS/Gateway returned an empty response for account lookup");
            }

            return accountResponse;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} for account lookup", e.getStatusCode().value(), e);
            throw new CoreBankingException("CBS/Gateway returned an error response for account lookup", e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with CBS/Gateway for account lookup", e);
            throw new CoreBankingCommunicationException("Could not complete CBS/Gateway account lookup", e);
        }
    }
}

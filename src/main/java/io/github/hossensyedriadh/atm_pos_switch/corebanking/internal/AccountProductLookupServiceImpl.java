package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountProductResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.AccountProductLookupService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Log4j2
@Service
class AccountProductLookupServiceImpl implements AccountProductLookupService {
    private final RestClient restClient;

    @Value("${core-banking.atm-gateway.base-url}")
    private String coreBankingAtmGatewayBaseUrl;

    @Autowired
    public AccountProductLookupServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CoreBankingAccountProductResponse lookupAccountProduct(Long id) {
        try {
            CoreBankingAccountProductResponse response = this.restClient.get().uri(this.coreBankingAtmGatewayBaseUrl + "/account-products/" + id)
                    .retrieve().body(CoreBankingAccountProductResponse.class);

            if (response == null) {
                log.error("CBS returned an empty response for account product lookup with ID: {}", id);
                throw new CoreBankingException("CBS returned an empty response for account product lookup");
            }

            return response;
        } catch (RestClientResponseException e) {
            log.error("CBS returned an error response for account product lookup", e);
            throw new CoreBankingException("CBS returned an error response for account product lookup", e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with CBS for account product lookup", e);
            throw new CoreBankingCommunicationException("Could no complete CBS account product lookup", e);
        }
    }
}

package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountBalanceResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.BalanceLookupService;
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
class BalanceLookupServiceImpl implements BalanceLookupService {
    private final RestClient restClient;

    @Value("${core-banking.atm-gateway.base-url}")
    private String coreBankingAtmGatewayBaseUrl;

    @Autowired
    public BalanceLookupServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CoreBankingAccountBalanceResponse lookupBalance(Long accountId) {
        try {
            CoreBankingAccountBalanceResponse response = this.restClient.get().uri(this.coreBankingAtmGatewayBaseUrl + "/accounts/" + accountId + "/balance")
                    .retrieve().body(CoreBankingAccountBalanceResponse.class);

            if (response == null) {
                log.error("CBS returned an empty response for balance lookup for account {}", accountId);
                throw new CoreBankingException("CBS returned an empty response for balance lookup");
            }

            return response;
        } catch (RestClientResponseException e) {
            log.error("CBS returned an error response for balance lookup", e);
            throw new CoreBankingException("CBS returned an error response for balance lookup", e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with CBS for balance lookup", e);
            throw new CoreBankingCommunicationException("Could no complete CBS balance lookup", e);
        }
    }
}

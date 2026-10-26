package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCurrencyResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CurrencyLookupService;
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
class CurrencyLookupServiceImpl implements CurrencyLookupService {
    private final RestClient restClient;

    @Value("${core-banking.atm-gateway.base-url}")
    private String coreBankingAtmGatewayBaseUrl;

    @Autowired
    public CurrencyLookupServiceImpl(@Qualifier("cbsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CoreBankingCurrencyResponse lookupCurrency(Long currencyId) {
        try {
            CoreBankingCurrencyResponse response = this.restClient.get().uri(this.coreBankingAtmGatewayBaseUrl + "/currencies/" + currencyId)
                    .retrieve().body(CoreBankingCurrencyResponse.class);

            if (response == null) {
                log.error("CBS returned an empty response for currency lookup");
                throw new CoreBankingException("CBS returned an empty response for currency lookup");
            }

            return response;
        } catch (RestClientResponseException e) {
            log.error("CBS returned an error response for currency lookup", e);
            throw new CoreBankingException("CBS returned an error response for currency lookup", e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with CBS for currency lookup", e);
            throw new CoreBankingCommunicationException("Cound not complete CBS currency lookup", e);
        }
    }
}

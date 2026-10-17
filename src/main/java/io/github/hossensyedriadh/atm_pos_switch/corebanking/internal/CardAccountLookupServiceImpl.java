package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardAccountAggregateResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CardAccountLookupService;
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
class CardAccountLookupServiceImpl implements CardAccountLookupService {
    private final RestClient restClient;

    @Value("${cbs.base-url}")
    private String cbsBaseUrl;

    @Autowired
    public CardAccountLookupServiceImpl(@Qualifier("cbsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CoreBankingCardAccountAggregateResult lookupCardAccount(String pan) {
        try {
            CoreBankingCardResponse cardResponse = this.restClient.get().uri(this.cbsBaseUrl + "/cards/v1/pan/" + pan)
                    .retrieve().body(CoreBankingCardResponse.class);

            if (cardResponse == null) {
                log.error("CBS returned an empty response for card lookup");
                throw new CoreBankingException("CBS returned an empty response for card lookup");
            }

            CoreBankingAccountResponse accountResponse = this.restClient.get().uri(this.cbsBaseUrl + "/accounts/v1/" + cardResponse.getAccountId())
                    .retrieve().body(CoreBankingAccountResponse.class);

            if (accountResponse == null) {
                log.error("CBS returned an empty response for account lookup");
                throw new CoreBankingException("CBS returned an empty response for account lookup");
            }

            return new CoreBankingCardAccountAggregateResult(cardResponse.getId(), cardResponse.getCardProductId(), cardResponse.getLastFour(), cardResponse.getStatus(),
                    cardResponse.getExpiryDate(), cardResponse.getAccountId(), accountResponse.getAccountNumber(), accountResponse.getProductId(), accountResponse.getBranchId(), accountResponse.getCurrencyId(),
                    accountResponse.getStatus());
        } catch (RestClientResponseException e) {
            log.error("CBS returned an error response for card-account lookup", e);
            throw new CoreBankingException("CBS returned an error response for card lookup", e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with CBS for card-account lookup", e);
            throw new CoreBankingCommunicationException("Could not complete CBS card lookup", e);
        }
    }
}

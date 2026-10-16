package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingReversalRequest;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingReversalResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingTransactionResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CoreBankingService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingException;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Log4j2
@Service
class CoreBankingServiceImpl implements CoreBankingService {
    private final RestClient restClient;

    @Value("${cbs.base-url}")
    private String cbsBaseUrl;

    @Autowired
    public CoreBankingServiceImpl(@Qualifier("cbsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CoreBankingReversalResult reverseTransaction(SwitchTransactionSummary originalTransaction, String reversalReference) {
        CoreBankingReversalRequest reversalRequest = new CoreBankingReversalRequest();
        reversalRequest.setTransactionId(originalTransaction.cbsTxnId());
        reversalRequest.setIdempotencyKey(reversalReference);
        reversalRequest.setDescription(String.format("Reversal of ATM/POS switch transaction ID: %d [Terminal: %s, STAN: %s, RRN: %s].",
                originalTransaction.id(), originalTransaction.terminalId(), originalTransaction.stan(), originalTransaction.retrievalReference()
        ));

        try {
            CoreBankingTransactionResponse reversalResponse = this.restClient.post().uri(this.cbsBaseUrl + "/transactions/v1/reversals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reversalRequest).retrieve()
                    .body(CoreBankingTransactionResponse.class);

            if (reversalResponse == null) {
                log.error("CBS returned an empty response for transaction reversal");
                throw new CoreBankingException("CBS returned an empty response");
            }

            boolean isSuccessful = reversalResponse.getStatus() == TransactionStatus.COMPLETED;

            return new CoreBankingReversalResult(isSuccessful, reversalResponse.getId(),
                    reversalResponse.getReversalOfTransactionId(), isSuccessful ? "00" : "96");
        } catch (RestClientResponseException e) {
            log.error("CBS rejected reversal request. Original switch transaction ID: {}, HTTP status: {}", originalTransaction.id(),
                    e.getStatusCode(), e
            );

            throw new CoreBankingException("CBS reversal request failed with HTTP status " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with CBS for transaction reversal. Original switch transaction ID: {}",
                    originalTransaction.id(), e);

            throw new CoreBankingCommunicationException("Could not complete CBS transaction reversal request", e);
        }
    }
}

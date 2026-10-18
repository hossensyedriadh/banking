package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.TransactionType;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.*;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CoreBankingService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingException;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
class CoreBankingServiceImpl implements CoreBankingService {
    private final RestClient restClient;
    private final CoreBankingResponseCodeMapper coreBankingResponseCodeMapper;

    @Value("${cbs.base-url}")
    private String cbsBaseUrl;

    @Autowired
    public CoreBankingServiceImpl(@Qualifier("cbsRestClient") RestClient restClient, CoreBankingResponseCodeMapper coreBankingResponseCodeMapper) {
        this.restClient = restClient;
        this.coreBankingResponseCodeMapper = coreBankingResponseCodeMapper;
    }

    @Override
    public CoreBankingWithdrawalResult withdraw(SwitchTransactionSummary transaction, Long accountId, Long currencyId, String idempotencyKey) {
        CoreBankingWithdrawalRequest withdrawalRequest = new CoreBankingWithdrawalRequest();
        withdrawalRequest.setAccountId(accountId);
        withdrawalRequest.setCurrencyId(currencyId);
        withdrawalRequest.setIdempotencyKey(idempotencyKey);
        withdrawalRequest.setAmount(transaction.transactionAmount());
        withdrawalRequest.setDescription(String.format("ATM cash withdrawal [Switch transaction ID: %d, Terminal: %s, STAN: %s, RRN: %s].", transaction.id(), transaction.terminalId(),
                transaction.stan(), transaction.retrievalReference()));

        try {
            CoreBankingTransactionResponse response = this.restClient.post().uri(this.cbsBaseUrl + "/transactions/v1/withdrawals")
                    .contentType(MediaType.APPLICATION_JSON).body(withdrawalRequest).retrieve()
                    .body(CoreBankingTransactionResponse.class);

            if (response == null) {
                log.error("CBS returned an empty withdrawal response. Switch transaction ID: {}", transaction.id());
                throw new CoreBankingCommunicationException("CBS returned an empty withdrawal response");
            }

            if (response.getStatus() == TransactionStatus.COMPLETED) {
                if (response.getId() == null) {
                    log.error("CBS reported a completed withdrawal without a transaction ID. Switch transaction ID: {}", transaction.id());
                    throw new CoreBankingCommunicationException("CBS returned an incomplete withdrawal response");
                }

                return new CoreBankingWithdrawalResult(true, response.getId(), ResponseCodes.APPROVED);
            }

            log.error("CBS returned a non-completed withdrawal status. Switch transaction ID: {}, CBS status: {}", transaction.id(), response.getStatus());
            throw new CoreBankingCommunicationException("CBS withdrawal outcome is not confirmed");
        } catch (RestClientResponseException e) {
            CoreBankingErrorResponse errorResponse = null;

            try {
                errorResponse = e.getResponseBodyAs(CoreBankingErrorResponse.class);
            } catch (RestClientException parseException) {
                log.warn("Could not deserialize CBS error response. Switch transaction ID: {}", transaction.id(), parseException);
            }

            Optional<String> responseCode = this.coreBankingResponseCodeMapper.toIsoResponseCode(errorResponse);

            if (responseCode.isPresent()) {
                log.info("CBS definitively declined withdrawal. Switch Transaction ID: {}, response code: {}", transaction.id(), responseCode.get());

                return new CoreBankingWithdrawalResult(false, null, responseCode.get());
            }

            log.error("CBS returned an unrecognized or ambiguous error during withdrawal. Switch transaction ID: {}, HTTP status: {}", transaction.id(), e.getStatusCode(), e);
            throw new CoreBankingCommunicationException("CBS withdrawal result could not be confirmed", e);
        } catch (RestClientException e) {
            log.error("CBS communication failure during withdrawal. Switch transaction ID: {}", transaction.id(), e);
            throw new CoreBankingCommunicationException("Could not confirm CBS withdrawal outcome", e);
        }
    }

    @Override
    public Optional<CoreBankingTransactionResponse> findTransactionByIdempotencyKey(String idempotencyKey) {
        try {
            CoreBankingTransactionResponse response = this.restClient.get().uri(this.cbsBaseUrl + "/transactions/v1/idempotency/{key}", idempotencyKey)
                    .retrieve().body(CoreBankingTransactionResponse.class);

            if (response == null) {
                log.error("CBS returned an empty response for transaction lookup by idempotency key");
                throw new CoreBankingCommunicationException("CBS returned an empty response for transaction lookup by idempotency key");
            }

            return Optional.of(response);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientResponseException e) {
            log.error("CBS returned an error during idempotency lookup", e);
            throw new CoreBankingCommunicationException("Could not determine CBS transaction outcome", e);
        } catch (RestClientException e) {
            log.error("CBS communication failure during idempotency lookup", e);
            throw new CoreBankingCommunicationException("Could not determine CBS transaction outcome", e);
        }
    }

    @Override
    public CoreBankingReversalResult reverseTransaction(SwitchTransactionSummary originalTransaction, String reversalReference) {
        if (originalTransaction.cbsTxnId() == null) {
            log.error("Cannot reverse a switch transaction without a CBS transaction ID");
            throw new CoreBankingException("Cannot reverse a switch transaction without a CBS transaction ID");
        }

        CoreBankingReversalRequest reversalRequest = new CoreBankingReversalRequest();
        reversalRequest.setTransactionId(originalTransaction.cbsTxnId());
        reversalRequest.setIdempotencyKey(reversalReference);
        reversalRequest.setDescription(String.format("Reversal of ATM/POS switch transaction ID: %d [Terminal: %s, STAN: %s, RRN: %s].",
                originalTransaction.id(), originalTransaction.terminalId(), originalTransaction.stan(), originalTransaction.retrievalReference()));

        try {
            CoreBankingTransactionResponse reversalResponse = this.restClient.post().uri(this.cbsBaseUrl + "/transactions/v1/reversals")
                    .contentType(MediaType.APPLICATION_JSON).body(reversalRequest).retrieve()
                    .body(CoreBankingTransactionResponse.class);

            if (reversalResponse == null) {
                log.error("CBS returned an empty response for transaction reversal");
                throw new CoreBankingCommunicationException("CBS returned an empty response for transaction reversal");
            }

            if (reversalResponse.getId() == null || !Objects.equals(reversalResponse.getReversalOfTransactionId(), originalTransaction.cbsTxnId())) {
                log.error("CBS returned an incomplete or inconsistent reversal response");
                throw new CoreBankingCommunicationException("CBS returned an incomplete or inconsistent reversal response");
            }

            if (reversalResponse.getTransactionType() != TransactionType.REVERSAL) {
                log.error("CBS returned an unexpected transaction type {} for reversal", reversalResponse.getTransactionType());
                throw new CoreBankingCommunicationException("CBS returned an unexpected transaction type for reversal");
            }

            String returnedIdempotencyKey = reversalResponse.getIdempotencyKey();

            if (returnedIdempotencyKey != null && !returnedIdempotencyKey.isBlank()
                    && !reversalReference.equals(returnedIdempotencyKey)) {
                log.error("CBS returned a mismatched reversal idempotency key");
                throw new CoreBankingCommunicationException("CBS returned a mismatched reversal idempotency key");
            }

            if (reversalResponse.getStatus() != TransactionStatus.COMPLETED) {
                log.error("CBS did not confirm reversal completion; status=" + reversalResponse.getStatus());
                throw new CoreBankingCommunicationException("CBS did not confirm reversal completion; status=" + reversalResponse.getStatus());
            }

            return new CoreBankingReversalResult(true, reversalResponse.getId(), reversalResponse.getReversalOfTransactionId(), ResponseCodes.APPROVED);
        } catch (RestClientResponseException e) {
            log.error("CBS returned HTTP error during reversal. Switch transaction ID: {}, HTTP status: {}", originalTransaction.id(), e.getStatusCode(), e);
            throw new CoreBankingCommunicationException("CBS reversal outcome could not be confirmed", e);
        } catch (RestClientException e) {
            log.error("Communication failure during CBS reversal. Switch transaction ID: {}", originalTransaction.id(), e);
            throw new CoreBankingCommunicationException("CBS reversal outcome could not be confirmed", e);
        }
    }
}

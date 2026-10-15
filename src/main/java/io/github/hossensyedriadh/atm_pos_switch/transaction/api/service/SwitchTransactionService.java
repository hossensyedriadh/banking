package io.github.hossensyedriadh.atm_pos_switch.transaction.api.service;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionRegistrationResult;

import java.time.Duration;
import java.util.Optional;

public interface SwitchTransactionService {
    void markProcessing(long transactionId);

    void complete(long transactionId, String responseCode, TransactionStatus status, byte[] responsePayload);

    void markFailed(long transactionId, TransactionStatus status);

    int recoverStuckTransactions(Duration timeout);

    SwitchTransactionRegistrationResult registerRequest(String terminalId, String stan, String retrievalReference,
                                                         String processingCode, String requestMti, String transmissionDateTime);

    Optional<byte[]> findResponsePayload(long transactionId);
}

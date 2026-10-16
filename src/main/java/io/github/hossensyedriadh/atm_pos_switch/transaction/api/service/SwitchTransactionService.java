package io.github.hossensyedriadh.atm_pos_switch.transaction.api.service;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.OriginalDataElements;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionRegistrationResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

public interface SwitchTransactionService {
    void markProcessing(long transactionId);

    void complete(long transactionId, String responseCode, TransactionStatus status, byte[] responsePayload);

    void markFailed(long transactionId, TransactionStatus status);

    int recoverStuckTransactions(Duration timeout);

    SwitchTransactionRegistrationResult registerRequest(String terminalId, String stan, String retrievalReference,
                                                        String processingCode, String requestMti, String transmissionDateTime);

    Optional<Long> registerReversalRequest(String terminalId, String stan, String retrievalReference, String processingCode,
                                           String requestMti, String transmissionDateTime, long originalTransactionId);

    void updateCbsTransactionIds(long id, Long cbsTxnId, @Nullable Long cbsReversalTxnId);

    Optional<byte[]> findResponsePayload(long transactionId);

    Optional<TransactionStatus> findTransactionStatus(long transactionId);

    Optional<SwitchTransactionSummary> findTransaction(String terminalId, String stan, String transmissionDateTime);

    Optional<SwitchTransactionSummary> findOriginalTransaction(String terminalId, OriginalDataElements originalData);

    boolean hasReversal(long originalTransactionId);
}

package io.github.hossensyedriadh.atm_pos_switch.transaction.api.service;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface SwitchTransactionService {
    int markProcessing(long transactionId);

    TransactionCompletionResult complete(long transactionId, String responseCode, TransactionStatus status, byte[] responsePayload);

    void markFailed(long transactionId, TransactionStatus status);

    int recoverStuckTransactions(Duration timeout);

    SwitchTransactionRegistrationResult registerRequest(String terminalId, String stan, String retrievalReference,
                                                        String processingCode, String requestMti, String transmissionDateTime, BigDecimal transactionAmount,
                                                        String currencyCode, byte[] requestPayload);

    ReversalRegistrationResult registerReversalRequest(String terminalId, String stan, String retrievalReference, String processingCode,
                                                       String requestMti, String transmissionDateTime, long originalTransactionId);

    void updateCbsAccountAndCurrencyIds(long transactionId, Long accountId, Long currencyId);

    void updateCbsTransactionIds(long id, Long cbsTxnId, @Nullable Long cbsReversalTxnId);

    Optional<Long> findCbsCurrencyId(long transactionId);

    List<SwitchTransactionSummary> findUnknownWithdrawals(int batchSize);

    List<SwitchTransactionSummary> findUnknownReversals(int batchSize);

    int resolveUnknownTransaction(long id, String responseCode, TransactionStatus resolvedStatus, Long cbsTransactionId, byte[] responsePayload);

    int resolveUnknownReversalAndUpdateOriginal(long reversalTransactionId, long originalCbsTransactionId, String responseCode, Long cbsReversalTransactionId, byte[] responsePayload);

    int completeReversalAtomically(long reversalTransactionId, long originalTransactionId, Long originalCbsTransactionId, String responseCode, TransactionStatus resolvedStatus, Long cbsReversalTransactionId, byte[] responsePayload);

    Optional<byte[]> findResponsePayload(long transactionId);

    Optional<byte[]> findRequestPayload(long transactionId);

    Optional<TransactionStatus> findTransactionStatus(long transactionId);

    Optional<SwitchTransactionSummary> findTransactionById(long transactionId);

    Optional<Long> findOriginalTransactionId(long reversalTransactionId);

    Optional<SwitchTransactionSummary> findTransaction(String terminalId, String stan, String transmissionDateTime);

    Optional<SwitchTransactionSummary> findOriginalTransaction(String terminalId, OriginalDataElements originalData);

    boolean hasReversal(long originalTransactionId);
}

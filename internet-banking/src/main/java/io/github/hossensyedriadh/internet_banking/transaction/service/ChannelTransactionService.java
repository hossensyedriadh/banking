package io.github.hossensyedriadh.internet_banking.transaction.service;

import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsTransactionResponse;
import io.github.hossensyedriadh.internet_banking.transaction.entity.IbankingTransaction;
import io.github.hossensyedriadh.internet_banking.transaction.model.FundTransferRequest;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionAcquisition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelTransactionService {
    boolean existsWithIdempotencyKeyAndCustomerId(UUID key, Long customerId);

    Optional<IbankingTransaction> findById(Long id);

    Optional<IbankingTransaction> findByIdempotencyKeyAndCustomerId(UUID key, Long customerId);

    TransactionAcquisition createTransaction(UUID idempotencyKey, FundTransferRequest request);

    void recordCbsResult(Long id, CbsTransactionResponse cbsTransactionResponse);

    void markTransactionUnknown(Long id);

    void markTransactionFailed(Long id);

    boolean claimForProcessing(Long transactionId);

    boolean claimForReconciliation(Long transactionId);

    List<Long> findUnknownTransactionIds(int limit);
}

package io.github.hossensyedriadh.internet_banking.transaction.service;

public interface TransactionReconciliationService {
    void reconcile(Long channelTransactionId);
}

package io.github.hossensyedriadh.atm_pos_switch.transaction.api.service;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionRegistrationResult;

public interface SwitchTransactionService {
    long recordReceived(String terminalId, String stan, String retrievalReference,
                        String processingCode, String requestMti, String transmissionDateTime);

    void markProcessing(long transactionId);

    void complete(long transactionId, String responseCode, TransactionStatus status);

    SwitchTransactionRegistrationResult registerRequest(String terminalId, String stan, String retrievalReference,
                                                         String processingCode, String requestMti, String transmissionDateTime);
}

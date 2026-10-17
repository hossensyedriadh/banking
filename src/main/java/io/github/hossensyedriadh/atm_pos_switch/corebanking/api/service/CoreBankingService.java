package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingReversalResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingWithdrawalResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;

public interface CoreBankingService {
    CoreBankingWithdrawalResult withdraw(SwitchTransactionSummary transaction, Long accountId, Long currencyId, String idempotencyKey);

    CoreBankingReversalResult reverseTransaction(SwitchTransactionSummary originalTransaction, String reversalReference);
}

package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingReversalResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;

public interface CoreBankingService {
    CoreBankingReversalResult reverseTransaction(SwitchTransactionSummary originalTransaction, String reversalReference);
}

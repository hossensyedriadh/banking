package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingReversalResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingTransactionResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingWithdrawalResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;

import java.util.Optional;

public interface CoreBankingService {
    CoreBankingWithdrawalResult withdraw(SwitchTransactionSummary transaction, Long accountId, Long currencyId, String idempotencyKey);

    Optional<CoreBankingTransactionResponse> findTransactionByIdempotencyKey(String idempotencyKey);

    CoreBankingReversalResult reverseTransaction(SwitchTransactionSummary originalTransaction, String reversalReference);
}

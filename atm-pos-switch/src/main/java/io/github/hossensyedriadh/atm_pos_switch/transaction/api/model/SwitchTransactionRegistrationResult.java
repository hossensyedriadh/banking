package io.github.hossensyedriadh.atm_pos_switch.transaction.api.model;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionRegistrationOutcome;

public record SwitchTransactionRegistrationResult(long transactionId, TransactionRegistrationOutcome outcome) {
}

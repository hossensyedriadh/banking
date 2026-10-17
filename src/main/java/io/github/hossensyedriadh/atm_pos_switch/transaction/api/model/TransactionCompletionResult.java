package io.github.hossensyedriadh.atm_pos_switch.transaction.api.model;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;

public record TransactionCompletionResult(boolean completed, TransactionStatus currentStatus, byte[] responsePayload) {
}

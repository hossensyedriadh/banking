package io.github.hossensyedriadh.atm_pos_switch.transaction.api.model;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.ReversalRegistrationOutcome;

public record ReversalRegistrationResult(Long transactionId, ReversalRegistrationOutcome outcome) {
}

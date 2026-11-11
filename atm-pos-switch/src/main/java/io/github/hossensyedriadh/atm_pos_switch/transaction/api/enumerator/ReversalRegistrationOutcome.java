package io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator;

public enum ReversalRegistrationOutcome {
    REGISTERED,
    DUPLICATE_REQUEST,
    ORIGINAL_ALREADY_REVERSED,
    REQUEST_IDENTITY_CONFLICT
}

package io.github.hossensyedriadh.cbs.cheque.api.enumerator;

public enum ChequeReturnReason {
    INSUFFICIENT_FUNDS,
    ACCOUNT_CLOSED,
    ACCOUNT_BLOCKED,
    CHEQUE_STOPPED,
    CHEQUE_CANCELLED,
    CHEQUE_EXPIRED,
    INVALID_CHEQUE,
    SIGNATURE_MISMATCH,
    OTHER
}

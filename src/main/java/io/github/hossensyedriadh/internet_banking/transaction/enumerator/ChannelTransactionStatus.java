package io.github.hossensyedriadh.internet_banking.transaction.enumerator;

import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.CbsTransactionStatus;

public enum ChannelTransactionStatus {
    RECEIVED,
    PROCESSING,
    APPROVED,
    DECLINED,
    FAILED,
    UNKNOWN;

    public boolean isFinal() {
        return switch (this) {
            case APPROVED, DECLINED, FAILED -> true;
            default -> false;
        };
    }

    public static ChannelTransactionStatus mapFromCbsTransactionStatus(CbsTransactionStatus status) {
        return switch (status) {
            case PENDING -> PROCESSING;
            case COMPLETED -> APPROVED;
            case FAILED -> DECLINED;
            case REVERSED -> FAILED;
        };
    }
}
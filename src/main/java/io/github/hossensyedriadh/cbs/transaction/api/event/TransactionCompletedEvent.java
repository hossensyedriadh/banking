package io.github.hossensyedriadh.cbs.transaction.api.event;

import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;

public record TransactionCompletedEvent(Long transactionId, String reference, TransactionType type) {
}

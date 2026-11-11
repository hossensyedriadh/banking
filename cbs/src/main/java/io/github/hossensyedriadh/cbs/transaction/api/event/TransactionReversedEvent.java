package io.github.hossensyedriadh.cbs.transaction.api.event;

import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;

public record TransactionReversedEvent(Long originalTransactionId, Long reversalTransactionId, String reference, TransactionType type) {
}

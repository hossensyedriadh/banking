package io.github.hossensyedriadh.cbs.transaction.internal.event;

import io.github.hossensyedriadh.cbs.transaction.event.TransactionCreated;
import io.github.hossensyedriadh.cbs.transaction.event.TransactionReversed;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.Transaction;

public interface TransactionEventFactory {
    TransactionCreated createTransactionCreatedEvent(Transaction transaction);

    TransactionReversed createTransactionReversedEvent(Transaction transaction);
}

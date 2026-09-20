package io.github.hossensyedriadh.cbs.transaction.internal.event;

import io.github.hossensyedriadh.cbs.transaction.event.TransactionCreated;
import io.github.hossensyedriadh.cbs.transaction.event.TransactionReversed;

public interface TransactionEventProducer {
    void publish(TransactionCreated event);

    void publish(TransactionReversed event);
}

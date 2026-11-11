package io.github.hossensyedriadh.cbs.transaction.internal.event;

import org.apache.avro.specific.SpecificRecord;

public interface TransactionEventProducer {
    void publish(String key, SpecificRecord event);
}

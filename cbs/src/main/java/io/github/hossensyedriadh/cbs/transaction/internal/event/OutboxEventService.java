package io.github.hossensyedriadh.cbs.transaction.internal.event;

import org.apache.avro.specific.SpecificRecord;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface OutboxEventService {
    void save(UUID eventId, SpecificRecord event, String aggregateType, String aggregateId, OffsetDateTime occurredAt);
}

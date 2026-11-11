package io.github.hossensyedriadh.cbs.transaction.internal.event;

public interface OutboxEventPublisher {
    void publish(Long outboxEventId);
}

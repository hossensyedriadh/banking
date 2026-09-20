package io.github.hossensyedriadh.cbs.transaction.internal.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OutboxEventCreatedListener {
    private final OutboxEventPublisher outboxEventPublisher;

    @Autowired
    public OutboxEventCreatedListener(OutboxEventPublisher outboxEventPublisher) {
        this.outboxEventPublisher = outboxEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OutboxEventCreatedEvent event) {
        this.outboxEventPublisher.publish(event.outboxEventId());
    }
}

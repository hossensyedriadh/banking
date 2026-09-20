package io.github.hossensyedriadh.cbs.transaction.internal.event;

import io.github.hossensyedriadh.cbs.service.avro.AvroEventSerializer;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.OutboxEvent;
import io.github.hossensyedriadh.cbs.transaction.internal.repository.OutboxEventRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Log4j2
@Service
class OutboxEventServiceImpl implements OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final AvroEventSerializer avroEventSerializer;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public OutboxEventServiceImpl(OutboxEventRepository outboxEventRepository, AvroEventSerializer avroEventSerializer,
                                  ApplicationEventPublisher applicationEventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.avroEventSerializer = avroEventSerializer;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void save(UUID eventId, SpecificRecord event, String aggregateType, String aggregateId, OffsetDateTime occurredAt) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId(eventId);
        outboxEvent.setAggregateType(aggregateType);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(event.getSchema().getFullName());
        outboxEvent.setPayload(avroEventSerializer.serialize(event));
        outboxEvent.setOccurredAt(occurredAt);

        OutboxEvent saved = this.outboxEventRepository.save(outboxEvent);

        this.applicationEventPublisher.publishEvent(new OutboxEventCreatedEvent(saved.getId()));

        log.info("Saved outbox event [ID: {}] for event id: {}, type: {}", saved.getId(), eventId, event.getSchema().getFullName());
    }
}

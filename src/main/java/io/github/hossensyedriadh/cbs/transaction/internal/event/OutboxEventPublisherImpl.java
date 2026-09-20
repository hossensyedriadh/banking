package io.github.hossensyedriadh.cbs.transaction.internal.event;

import io.github.hossensyedriadh.cbs.transaction.event.TransactionCreated;
import io.github.hossensyedriadh.cbs.transaction.event.TransactionReversed;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.OutboxEvent;
import io.github.hossensyedriadh.cbs.transaction.internal.repository.OutboxEventRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Log4j2
@Component
class OutboxEventPublisherImpl implements OutboxEventPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionEventProducer eventProducer;

    @Autowired
    public OutboxEventPublisherImpl(OutboxEventRepository outboxEventRepository, @Qualifier("kafkaTransactionEventProducerImpl") TransactionEventProducer eventProducer) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    public void publish(Long outboxEventId) {
        OutboxEvent outboxEvent = this.outboxEventRepository.findById(outboxEventId).orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + outboxEventId));

        if (outboxEvent.getPublishedAt() != null) {
            log.debug("Outbox event {} has already been published", outboxEvent.getEventId());
            return;
        }

        SpecificRecord event = this.deserialize(outboxEvent.getEventType(), outboxEvent.getPayload());

        try {
            this.publishToKafka(outboxEvent, event);
            outboxEvent.setPublishedAt(OffsetDateTime.now());

            this.outboxEventRepository.save(outboxEvent);

            log.info("Outbox event published successfully: eventId={}, type={}", outboxEvent.getEventId(), outboxEvent.getEventType());

        } catch (Exception exception) {
            log.error("Failed to publish outbox event: eventId={}, type={}", outboxEvent.getEventId(), outboxEvent.getEventType(), exception);
        }
    }

    private void publishToKafka(OutboxEvent outboxEvent, SpecificRecord event) {
        String key = outboxEvent.getAggregateId();

        this.eventProducer.publish(key, event);
    }

    private SpecificRecord deserialize(String eventType, byte[] payload) {
        try {
            return switch (eventType) {
                case "io.github.hossensyedriadh.cbs.transaction.event.TransactionCreated" ->
                        this.deserialize(payload, TransactionCreated.getClassSchema());

                case "io.github.hossensyedriadh.cbs.transaction.event.TransactionReversed" ->
                        this.deserialize(payload, TransactionReversed.getClassSchema());

                default -> throw new IllegalArgumentException("Unsupported outbox event type: " + eventType);
            };

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to deserialize outbox event: " + eventType, exception);
        }
    }

    private <T extends SpecificRecord> T deserialize(byte[] payload, Schema schema) throws IOException {
        SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(payload, null);

        return reader.read(null, decoder);
    }
}

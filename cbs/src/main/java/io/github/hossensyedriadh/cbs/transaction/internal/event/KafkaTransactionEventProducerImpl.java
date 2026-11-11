package io.github.hossensyedriadh.cbs.transaction.internal.event;

import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Log4j2
@Component
class KafkaTransactionEventProducerImpl implements TransactionEventProducer {
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    private static final String TRANSACTION_EVENTS_TOPIC = "cbs.transaction.events";

    @Autowired
    public KafkaTransactionEventProducerImpl(KafkaTemplate<String, SpecificRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String key, SpecificRecord event) {
        try {
            this.kafkaTemplate.send(TRANSACTION_EVENTS_TOPIC, key, event).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException("Kafka publishing interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Kafka publishing failed", e.getCause());
        }
    }
}

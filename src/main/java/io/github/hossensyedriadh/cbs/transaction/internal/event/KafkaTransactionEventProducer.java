package io.github.hossensyedriadh.cbs.transaction.internal.event;

import io.github.hossensyedriadh.cbs.transaction.event.TransactionCreated;
import io.github.hossensyedriadh.cbs.transaction.event.TransactionReversed;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class KafkaTransactionEventProducer implements TransactionEventProducer {
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    private static final String TRANSACTION_EVENTS_TOPIC = "cbs.transaction.events";

    @Autowired
    public KafkaTransactionEventProducer(KafkaTemplate<String, SpecificRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(TransactionCreated event) {
        this.kafkaTemplate.send(TRANSACTION_EVENTS_TOPIC, String.valueOf(event.getTransactionId()), event);
    }

    @Override
    public void publish(TransactionReversed event) {
        this.kafkaTemplate.send(TRANSACTION_EVENTS_TOPIC, String.valueOf(event.getTransactionId()), event);
    }
}

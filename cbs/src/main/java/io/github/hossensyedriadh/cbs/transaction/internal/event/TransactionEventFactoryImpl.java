package io.github.hossensyedriadh.cbs.transaction.internal.event;

import io.github.hossensyedriadh.cbs.transaction.event.TransactionCreated;
import io.github.hossensyedriadh.cbs.transaction.event.TransactionReversed;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class TransactionEventFactoryImpl implements TransactionEventFactory {
    @Override
    public TransactionCreated createTransactionCreatedEvent(Transaction transaction) {
        return TransactionCreated.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTransactionId(transaction.getId())
                .setTransactionReference(transaction.getTransactionReference())
                .setTransactionType(transaction.getTransactionType().toString())
                .setStatus(transaction.getStatus().toString())
                .setAccountId(transaction.getAccountId())
                .setDestinationAccountId(transaction.getDestinationAccountId())
                .setAmount(transaction.getAmount().toPlainString())
                .setCurrencyId(transaction.getCurrencyId())
                .setTransactionType(transaction.getTransactionType().toString())
                .setAmount(transaction.getAmount().toPlainString())
                .setStatus(transaction.getStatus().toString())
                .setDescription(transaction.getDescription())
                .setOccurredAt(transaction.getCreatedAt().toInstant())
                .setIdempotencyKey(transaction.getIdempotencyKey())
                .build();
    }

    @Override
    public TransactionReversed createTransactionReversedEvent(Transaction transaction) {
        return TransactionReversed.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTransactionId(transaction.getId())
                .setTransactionReference(transaction.getTransactionReference())
                .setReversalOfTransactionId(transaction.getReversalOfTransactionId())
                .setAccountId(transaction.getAccountId())
                .setDestinationAccountId(transaction.getDestinationAccountId())
                .setCurrencyId(transaction.getCurrencyId())
                .setTransactionType(transaction.getTransactionType().toString())
                .setAmount(transaction.getAmount().toPlainString())
                .setStatus(transaction.getStatus().toString())
                .setDescription(transaction.getDescription())
                .setOccurredAt(transaction.getCreatedAt().toInstant())
                .setIdempotencyKey(transaction.getIdempotencyKey())
                .build();
    }
}

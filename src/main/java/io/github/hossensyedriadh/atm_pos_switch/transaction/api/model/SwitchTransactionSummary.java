package io.github.hossensyedriadh.atm_pos_switch.transaction.api.model;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SwitchTransactionSummary(long id, String terminalId, String stan, String retrievalReference,
                                       String processingCode, BigDecimal transactionAmount, String currencyCode,
                                       String requestMti, String responseCode, TransactionStatus status,
                                       OffsetDateTime receivedAt, OffsetDateTime processingStartedAt,
                                       OffsetDateTime completedAt) {
}

package io.github.hossensyedriadh.atm_pos_switch.transaction.api.model;

import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SwitchTransaction(long id, String terminalId, String stan, String retrievalReference,
                                String processingCode, BigDecimal transactionAmount, String currencyCode,
                                String requestMti, String responseCode, TransactionStatus status,
                                OffsetDateTime receivedAt, OffsetDateTime createdAt) {
}

package io.github.hossensyedriadh.atm_pos_switch.transaction.api.model;

import java.math.BigDecimal;

public record DuplicateTransactionCandidate(long id, String retrievalReference, String processingCode,
                                            String requestMti,
                                            BigDecimal transactionAmount, String currencyCode) {
}

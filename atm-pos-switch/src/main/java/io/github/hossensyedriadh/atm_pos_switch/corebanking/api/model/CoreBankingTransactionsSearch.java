package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.TransactionType;

import java.time.OffsetDateTime;

public record CoreBankingTransactionsSearch(OffsetDateTime from, OffsetDateTime to,
                                            TransactionType type, TransactionStatus status,
                                            Long accountId, int page, int size) {
}

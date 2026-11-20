package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.TransactionStatus;
import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.TransactionType;

import java.time.OffsetDateTime;

public record CbsTransactionsSearch(OffsetDateTime from, OffsetDateTime to,
                                    TransactionType type, TransactionStatus status,
                                    Long accountId, int page, int size) {
}

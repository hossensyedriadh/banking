package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.CbsTransactionStatus;
import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.TransactionType;

import java.time.OffsetDateTime;

public record CbsTransactionsSearch(OffsetDateTime from, OffsetDateTime to,
                                    TransactionType type, CbsTransactionStatus status,
                                    Long accountId, int page, int size) {
}

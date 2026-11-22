package io.github.hossensyedriadh.internet_banking.transaction.model;

import io.github.hossensyedriadh.internet_banking.transaction.entity.IbankingTransaction;

public record TransactionAcquisition(IbankingTransaction transaction, boolean created) {
}

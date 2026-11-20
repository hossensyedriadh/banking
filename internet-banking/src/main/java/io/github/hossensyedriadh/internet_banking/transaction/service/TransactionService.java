package io.github.hossensyedriadh.internet_banking.transaction.service;

import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsTransactionsSearch;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionModel;
import org.springframework.data.domain.Page;

public interface TransactionService {
    Page<TransactionModel> fetchTransactions(CbsTransactionsSearch search);

    TransactionModel fetchTransaction(Long id);
}

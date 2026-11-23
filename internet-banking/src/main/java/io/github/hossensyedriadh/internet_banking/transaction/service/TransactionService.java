package io.github.hossensyedriadh.internet_banking.transaction.service;

import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsTransactionsSearch;
import io.github.hossensyedriadh.internet_banking.transaction.model.FundTransferRequest;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionModel;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface TransactionService {
    Page<TransactionModel> fetchTransactions(CbsTransactionsSearch search);

    TransactionModel fetchTransaction(Long id);

    TransactionModel transfer(UUID idempotencyKey, @Valid FundTransferRequest request);
}

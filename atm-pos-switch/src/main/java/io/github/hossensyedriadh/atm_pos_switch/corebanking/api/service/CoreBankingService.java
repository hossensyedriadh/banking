package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.*;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CoreBankingService {
    CoreBankingWithdrawalResult withdraw(SwitchTransactionSummary transaction, Long accountId, Long currencyId, String idempotencyKey);

    CoreBankingFundTransferResult transfer(SwitchTransactionSummary transaction, Long accountId, Long destinationAccountId, Long currencyId, String idempotencyKey);

    CoreBankingReversalResult reverseTransaction(SwitchTransactionSummary originalTransaction, String reversalReference);

    Optional<CoreBankingTransactionResponse> findTransactionByIdempotencyKey(String idempotencyKey);

    CoreBankingTransactionPage findTransactions(CoreBankingTransactionsSearch search);

    default List<CoreBankingTransactionResponse> findAllTransactions(CoreBankingTransactionsSearch search) {
        CoreBankingTransactionPage firstPage = this.findTransactions(search);
        List<CoreBankingTransactionResponse> transactions = new ArrayList<>(firstPage.content());

        for (int page = 0; page < firstPage.totalPages(); page++) {
            CoreBankingTransactionsSearch nextSearch = new CoreBankingTransactionsSearch(search.from(), search.to(),
                    search.type(), search.status(), search.accountId(), page, search.size());

            CoreBankingTransactionPage nextPage = this.findTransactions(nextSearch);
            transactions.addAll(nextPage.content());
        }

        return List.copyOf(transactions);
    }
}

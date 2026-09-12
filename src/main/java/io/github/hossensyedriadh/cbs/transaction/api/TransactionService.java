package io.github.hossensyedriadh.cbs.transaction.api;

import io.github.hossensyedriadh.cbs.transaction.api.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionService {
    Page<AccountTransactionView> getAccountTransactions(Long accountId, Pageable pageable);

    Optional<TransactionView> getTransaction(Long id);

    Optional<TransactionView> getTransactionByReference(String reference);

    TransactionView deposit(CreateDepositCommand command);

    TransactionView withdraw(CreateWithdrawalCommand command);

    TransactionView transfer(CreateTransferCommand command);

    TransactionView reverse(ReverseTransactionCommand command);
}

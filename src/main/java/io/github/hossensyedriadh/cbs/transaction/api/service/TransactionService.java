package io.github.hossensyedriadh.cbs.transaction.api.service;

import io.github.hossensyedriadh.cbs.transaction.api.command.*;
import io.github.hossensyedriadh.cbs.transaction.api.model.AccountTransactionView;
import io.github.hossensyedriadh.cbs.transaction.api.model.TransactionView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Page<AccountTransactionView> getAccountTransactions(Long accountId, Pageable pageable);

    List<TransactionView> getTransactions(Long accountId, OffsetDateTime from, OffsetDateTime to);

    Optional<TransactionView> getTransaction(Long id);

    Optional<TransactionView> getTransactionByReference(String reference);

    Optional<TransactionView> getTransactionByIdempotencyKey(String idempotencyKey);

    TransactionView deposit(@Valid CreateDepositCommand command);

    TransactionView withdraw(@Valid CreateWithdrawalCommand command);

    TransactionView transfer(@Valid CreateTransferCommand command);

    TransactionView reverse(@Valid ReverseTransactionCommand command);

    TransactionView charge(@Valid ChargeFeeCommand command);

    TransactionView interest(@Valid PostInterestCommand command);
}

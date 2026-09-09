package io.github.hossensyedriadh.cbs.ledger.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Optional;

public interface LedgerService {
    Optional<Long> getLedgerAccountByAccountId(Long accountId);

    void createSubLedgerAccount(Long accountId, Long ledgerAccountId);

    void postDeposit(Long transactionId, Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);

    void postWithdrawal(Long transactionId, Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);

    void postTransfer(Long transactionId, Long sourceLedgerAccountId, Long destinationLedgerAccountId, @NotNull @Positive BigDecimal amount);
}

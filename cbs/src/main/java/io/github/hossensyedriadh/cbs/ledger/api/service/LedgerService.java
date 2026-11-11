package io.github.hossensyedriadh.cbs.ledger.api.service;

import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface LedgerService {
    Optional<Long> getSubLedgerAccountIdByAccountId(Long accountId);

    Optional<SubLedgerAccountView> getSubLedgerAccountByAccountId(Long accountId);

    BigDecimal getBalanceAt(Long subLedgerAccountId, OffsetDateTime asOf);

    void createSubLedgerAccount(Long accountId, Long ledgerAccountId);

    void postDeposit(@NotNull Long transactionId, @NotNull Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);

    void postWithdrawal(@NotNull Long transactionId, @NotNull Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);

    void postCustomerTransfer(@NotNull Long transactionId, @NotNull Long sourceLedgerAccountId, @NotNull Long destinationLedgerAccountId, @NotNull BigDecimal fee, @NotNull @Positive BigDecimal amount);

    void postInterest(@NotNull Long accrualId, @NotNull Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);

    void postFee(@NotNull Long transactionId, @NotNull Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);
}

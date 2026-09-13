package io.github.hossensyedriadh.cbs.ledger.api;

import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Optional;

public interface LedgerService {
    Optional<Long> getLedgerAccountByAccountId(Long accountId);

    Optional<Long> getSubLedgerAccountIdByAccountId(Long accountId);

    Optional<SubLedgerAccountView> getSubLedgerAccountByAccountId(Long accountId);

    void createSubLedgerAccount(Long accountId, Long ledgerAccountId);

    void postDeposit(Long transactionId, Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);

    void postWithdrawal(Long transactionId, Long customerLedgerAccountId, @NotNull @Positive BigDecimal amount);

    void postCustomerTransfer(Long transactionId, Long sourceLedgerAccountId, Long destinationLedgerAccountId, @NotNull BigDecimal fee, @NotNull @Positive BigDecimal amount);
}

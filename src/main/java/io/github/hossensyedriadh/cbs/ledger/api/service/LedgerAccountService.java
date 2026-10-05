package io.github.hossensyedriadh.cbs.ledger.api.service;

import io.github.hossensyedriadh.cbs.ledger.api.command.CreateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.command.UpdateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.LedgerAccountView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface LedgerAccountService {
    List<LedgerAccountView> getLedgerAccounts();

    Optional<LedgerAccountView> getLedgerAccount(Long id);

    Optional<LedgerAccountView> getLedgerAccountByCode(String accountCode);

    LedgerAccountView createLedgerAccount(@Valid CreateLedgerAccountCommand command);

    LedgerAccountView updateLedgerAccount(Long id, @Valid UpdateLedgerAccountCommand command);

    void activateLedgerAccount(Long id);

    void deactivateLedgerAccount(Long id);
}

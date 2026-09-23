package io.github.hossensyedriadh.cbs.ledger.api.service;

import io.github.hossensyedriadh.cbs.ledger.api.command.CreateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.command.UpdateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.LedgerAccountView;
import jakarta.validation.Valid;

import java.util.Optional;

public interface LedgerAccountService {
    Optional<LedgerAccountView> getLedgerAccount(Long id);

    Optional<LedgerAccountView> getLedgerAccountByCode(String accountCode);

    LedgerAccountView createLedgerAccount(@Valid CreateLedgerAccountCommand command);

    LedgerAccountView updateLedgerAccount(Long id, @Valid UpdateLedgerAccountCommand command);

    void activateLedgerAccount(Long id);

    void deactivateLedgerAccount(Long id);
}

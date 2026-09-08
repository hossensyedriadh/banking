package io.github.hossensyedriadh.cbs.ledger.api;

import io.github.hossensyedriadh.cbs.ledger.api.model.CreateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.LedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.api.model.UpdateLedgerAccountCommand;

import java.util.Optional;

public interface LedgerAccountService {
    Optional<LedgerAccountView> getLedgerAccount(Long id);

    Optional<LedgerAccountView> getLedgerAccountByCode(String accountCode);

    LedgerAccountView createLedgerAccount(CreateLedgerAccountCommand command);

    Optional<LedgerAccountView> updateLedgerAccount(Long id, UpdateLedgerAccountCommand command);

    void activateLedgerAccount(Long id);

    void deactivateLedgerAccount(Long id);
}

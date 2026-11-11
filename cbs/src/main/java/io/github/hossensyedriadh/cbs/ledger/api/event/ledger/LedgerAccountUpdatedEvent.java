package io.github.hossensyedriadh.cbs.ledger.api.event.ledger;

public record LedgerAccountUpdatedEvent(Long ledgerAccountId, String accountCode) {
}

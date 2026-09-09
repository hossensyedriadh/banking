package io.github.hossensyedriadh.cbs.ledger.api.event.ledger;

public record SubLedgerAccountCreatedEvent(Long subLedgerId, Long accountId, String ledgerAccountCode) {
}

package io.github.hossensyedriadh.cbs.account.api.event;

public record AccountClosedEvent(Long accountId, String accountNumber, String reason) {
}

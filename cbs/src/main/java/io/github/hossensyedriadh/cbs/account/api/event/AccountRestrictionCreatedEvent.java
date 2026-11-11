package io.github.hossensyedriadh.cbs.account.api.event;

public record AccountRestrictionCreatedEvent(Long restrictionId, String reason) {
}

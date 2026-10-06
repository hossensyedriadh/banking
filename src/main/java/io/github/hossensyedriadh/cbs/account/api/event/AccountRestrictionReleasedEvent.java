package io.github.hossensyedriadh.cbs.account.api.event;

public record AccountRestrictionReleasedEvent(Long restrictionId, String reason) {
}

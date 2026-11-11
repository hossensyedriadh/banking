package io.github.hossensyedriadh.cbs.account.api.event;

import java.time.LocalDate;

public record ScheduledFeeCreatedEvent(Long id, LocalDate scheduledDate) {
}

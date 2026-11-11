package io.github.hossensyedriadh.cbs.account.api.event;

import java.time.LocalDate;

public record ScheduledFeeCancelledEvent(Long id, LocalDate scheduledDate) {
}

package io.github.hossensyedriadh.cbs.account.api.event;

import java.time.LocalDate;

public record ScheduledFeeWaivedEvent(Long id, LocalDate scheduledDate) {
}

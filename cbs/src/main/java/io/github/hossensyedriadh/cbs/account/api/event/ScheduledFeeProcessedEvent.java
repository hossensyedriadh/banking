package io.github.hossensyedriadh.cbs.account.api.event;

import java.time.LocalDate;

public record ScheduledFeeProcessedEvent(Long id, LocalDate scheduledDate) {
}

package io.github.hossensyedriadh.cbs.eod.api.event;

import java.time.LocalDate;

public record EodRunCompletedEvent(Long eodRunId, LocalDate businessDate) {
}

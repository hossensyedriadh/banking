package io.github.hossensyedriadh.cbs.eod.api.event;

import java.time.LocalDate;

public record EodRunFailedEvent(Long eodRunId, LocalDate businessDate) {
}

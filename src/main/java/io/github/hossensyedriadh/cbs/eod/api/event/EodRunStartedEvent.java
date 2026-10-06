package io.github.hossensyedriadh.cbs.eod.api.event;

import java.time.LocalDate;

public record EodRunStartedEvent(Long eodRunId, LocalDate businessDate) {
}

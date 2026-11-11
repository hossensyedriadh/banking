package io.github.hossensyedriadh.cbs.eod.api.event;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;

public record EodRunStepCompletedEvent(Long eodRunStepId, EodStep step) {
}

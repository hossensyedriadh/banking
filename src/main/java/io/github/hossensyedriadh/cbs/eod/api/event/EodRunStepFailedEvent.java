package io.github.hossensyedriadh.cbs.eod.api.event;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;

public record EodRunStepFailedEvent(Long eodRunStepId, EodStep step) {
}

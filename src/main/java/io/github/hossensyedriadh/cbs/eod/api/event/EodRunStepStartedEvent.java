package io.github.hossensyedriadh.cbs.eod.api.event;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;

public record EodRunStepStartedEvent(Long eodRunStepId, EodStep step) {
}

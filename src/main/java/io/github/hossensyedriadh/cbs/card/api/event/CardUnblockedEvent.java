package io.github.hossensyedriadh.cbs.card.api.event;

public record CardUnblockedEvent(Long cardId, String reason) {
}

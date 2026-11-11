package io.github.hossensyedriadh.cbs.card.api.event;

public record CardTemporaryBlockedEvent(Long cardId, String reason) {
}

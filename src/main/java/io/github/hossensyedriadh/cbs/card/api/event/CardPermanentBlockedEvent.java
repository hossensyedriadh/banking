package io.github.hossensyedriadh.cbs.card.api.event;

public record CardPermanentBlockedEvent(Long cardId, String reason) {
}

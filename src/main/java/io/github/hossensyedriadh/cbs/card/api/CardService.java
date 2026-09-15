package io.github.hossensyedriadh.cbs.card.api;

import io.github.hossensyedriadh.cbs.card.api.model.ActivateCardCommand;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.api.model.ChangeCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.model.IssueCardCommand;

import java.util.List;
import java.util.Optional;

public interface CardService {
    List<CardView> getCards(Long accountId);

    Optional<CardView> getCard(Long cardId);

    CardView activateCard(ActivateCardCommand command);

    CardView changePin(ChangeCardPinCommand command);

    CardView issueCard(IssueCardCommand command);

    CardView temporarilyBlockCard(Long cardId, String reason);

    CardView permanentlyBlockCard(Long cardId, String reason);

    CardView unblockCard(Long cardId, String reason);

    CardView closeCard(Long cardId, String reason);
}

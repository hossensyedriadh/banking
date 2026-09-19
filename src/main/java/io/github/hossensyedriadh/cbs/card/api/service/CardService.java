package io.github.hossensyedriadh.cbs.card.api.service;

import io.github.hossensyedriadh.cbs.card.api.command.ActivateCardCommand;
import io.github.hossensyedriadh.cbs.card.api.command.IssueCardCommand;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;

import java.util.List;
import java.util.Optional;

public interface CardService {
    List<CardView> getCards(Long accountId);

    Optional<CardView> getCard(Long cardId);

    CardView activateCard(ActivateCardCommand command);

    CardView issueCard(IssueCardCommand command);

    CardView temporarilyBlockCard(Long cardId, String reason);

    CardView permanentlyBlockCard(Long cardId, String reason);

    CardView unblockCard(Long cardId, String reason);

    CardView closeCard(Long cardId, String reason);
}

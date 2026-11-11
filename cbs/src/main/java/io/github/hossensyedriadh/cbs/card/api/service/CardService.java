package io.github.hossensyedriadh.cbs.card.api.service;

import io.github.hossensyedriadh.cbs.card.api.command.*;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface CardService {
    List<CardView> getCards(Long accountId);

    Optional<CardView> getCard(Long cardId);

    Optional<CardView> getCard(String pan);

    CardView activateCard(@Valid ActivateCardCommand command);

    CardView issueCard(@Valid IssueCardCommand command);

    CardView replaceCard(@Valid ReplaceCardCommand command);

    CardView temporarilyBlockCard(@Valid BlockCardCommand command);

    CardView permanentlyBlockCard(@Valid BlockCardCommand command);

    CardView unblockCard(@Valid UnblockCardCommand command);

    CardView closeCard(@Valid CloseCardCommand command);
}

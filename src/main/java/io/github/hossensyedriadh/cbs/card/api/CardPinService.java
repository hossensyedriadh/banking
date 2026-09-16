package io.github.hossensyedriadh.cbs.card.api;

import io.github.hossensyedriadh.cbs.card.api.enumerator.CardPinVerificationResult;
import io.github.hossensyedriadh.cbs.card.api.model.ChangeCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.model.SetCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.model.VerifyCardPinCommand;

public interface CardPinService {
    void setPin(SetCardPinCommand command);

    void changePin(ChangeCardPinCommand command);

    CardPinVerificationResult verifyPin(VerifyCardPinCommand command);

    void unblockPin(Long cardId);
}

package io.github.hossensyedriadh.cbs.card.api.service;

import io.github.hossensyedriadh.cbs.card.api.command.ChangeCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.command.SetCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.command.VerifyCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardPinVerificationResult;

public interface CardPinService {
    void setPin(SetCardPinCommand command);

    void changePin(ChangeCardPinCommand command);

    CardPinVerificationResult verifyPin(VerifyCardPinCommand command);

    void unblockPin(Long cardId);
}

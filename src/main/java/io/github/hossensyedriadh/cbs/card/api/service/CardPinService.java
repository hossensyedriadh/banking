package io.github.hossensyedriadh.cbs.card.api.service;

import io.github.hossensyedriadh.cbs.card.api.command.ChangeCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.command.SetCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.command.VerifyCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardPinVerificationResult;
import jakarta.validation.Valid;

public interface CardPinService {
    void setPin(@Valid SetCardPinCommand command);

    void changePin(@Valid ChangeCardPinCommand command);

    CardPinVerificationResult verifyPin(@Valid VerifyCardPinCommand command);

    void unblockPin(Long cardId);
}

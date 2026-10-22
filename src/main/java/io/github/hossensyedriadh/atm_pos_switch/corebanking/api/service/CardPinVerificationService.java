package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.CardPinVerificationResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardPinVerificationRequest;

public interface CardPinVerificationService {
    CardPinVerificationResult verify(CoreBankingCardPinVerificationRequest request);
}

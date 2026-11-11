package io.github.hossensyedriadh.cbs.card.api.service;

import io.github.hossensyedriadh.cbs.card.api.command.PinBlockVerificationCommand;

public interface PinBlockVerifier {
    boolean verify(PinBlockVerificationCommand command);
}

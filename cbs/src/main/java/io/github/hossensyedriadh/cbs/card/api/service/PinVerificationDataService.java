package io.github.hossensyedriadh.cbs.card.api.service;

public interface PinVerificationDataService {
    String createVerificationData(String pan, String pin);
}

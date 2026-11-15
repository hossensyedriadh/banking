package io.github.hossensyedriadh.cbs.authorization.api.service;

public interface BankingAuthorizationService {
    void authorizeAccountAccess(Long accountId);

    void authorizeCardAccess(Long accountId, Long cardId);

    Long getAuthenticatedCustomerId();
}

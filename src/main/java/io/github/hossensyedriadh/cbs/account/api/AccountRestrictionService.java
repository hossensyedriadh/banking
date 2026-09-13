package io.github.hossensyedriadh.cbs.account.api;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountRestrictionView;
import io.github.hossensyedriadh.cbs.account.api.model.CreateAccountRestrictionCommand;

import java.util.List;
import java.util.Optional;

public interface AccountRestrictionService {
    List<AccountRestrictionView> getRestrictions(Long accountId, AccountRestrictionStatus status);

    Optional<AccountRestrictionView> getRestriction(Long restrictionId);

    AccountRestrictionView createRestriction(CreateAccountRestrictionCommand command);

    AccountRestrictionView releaseRestriction(Long restrictionId);

    void validateDebitAllowed(Long accountId);

    void validateCreditAllowed(Long accountId);
}

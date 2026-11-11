package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.api.command.ReleaseAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountRestrictionView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface AccountRestrictionService {
    List<AccountRestrictionView> getRestrictions(Long accountId, AccountRestrictionStatus status);

    Optional<AccountRestrictionView> getRestriction(Long restrictionId);

    AccountRestrictionView createRestriction(@Valid CreateAccountRestrictionCommand command);

    AccountRestrictionView releaseRestriction(@Valid ReleaseAccountRestrictionCommand command);

    void validateDebitAllowed(Long accountId);

    void validateCreditAllowed(Long accountId);
}

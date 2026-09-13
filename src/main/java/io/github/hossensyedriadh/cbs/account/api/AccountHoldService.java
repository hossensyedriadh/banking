package io.github.hossensyedriadh.cbs.account.api;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldView;
import io.github.hossensyedriadh.cbs.account.api.model.CreateAccountHoldCommand;

import java.util.List;
import java.util.Optional;

public interface AccountHoldService {
    List<AccountHoldView> getHolds(Long accountId, AccountHoldStatus status);

    Optional<AccountHoldView> getHold(Long holdId);

    AccountHoldView createHold(CreateAccountHoldCommand command);

    AccountHoldView releaseHold(Long holdId);
}

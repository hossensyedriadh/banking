package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldView;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountHoldService {
    List<AccountHoldView> getHolds(Long accountId, AccountHoldStatus status);

    Optional<AccountHoldView> getHold(Long holdId);

    AccountHoldView createHold(@Valid CreateAccountHoldCommand command);

    AccountHoldView releaseHold(Long holdId);

    void expireHolds(LocalDate businessDate);
}

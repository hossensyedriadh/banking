package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.command.CreateFeeRuleCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.api.model.FeeRuleView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FeeRuleService {
    Page<FeeRuleView> getFeeRules(FeeRuleStatus status, Pageable pageable);

    List<FeeRuleView> getFeeRules(FeeRuleStatus status);

    Optional<FeeRuleView> getFeeRule(Long id);

    Optional<FeeRuleView> getFeeRule(String feeCode, FeeRuleStatus status);

    FeeRuleView createFeeRule(CreateFeeRuleCommand command);

    FeeRuleView activateFeeRule(Long id);

    FeeRuleView deactivateFeeRule(Long id);
}

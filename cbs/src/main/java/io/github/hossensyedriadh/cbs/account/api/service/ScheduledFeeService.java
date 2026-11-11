package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.command.ScheduleFeeCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import io.github.hossensyedriadh.cbs.account.api.model.ScheduledFeeView;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduledFeeService {
    Page<ScheduledFeeView> getScheduledFees(@Nullable ScheduledFeeStatus status, Pageable pageable);

    Page<ScheduledFeeView> getScheduledFees(Long accountId, ScheduledFeeStatus status, Pageable pageable);

    Optional<ScheduledFeeView> getScheduledFee(Long id);

    Optional<ScheduledFeeView> getScheduledFee(Long accountId, Long id);

    ScheduledFeeView schedule(@Valid ScheduleFeeCommand command);

    void processDueFees(LocalDate businessDate);

    void cancelScheduledFee(Long id);

    void waiveScheduledFee(Long id);
}

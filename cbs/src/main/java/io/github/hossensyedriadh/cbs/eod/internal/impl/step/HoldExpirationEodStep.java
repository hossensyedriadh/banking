package io.github.hossensyedriadh.cbs.eod.internal.impl.step;

import io.github.hossensyedriadh.cbs.account.api.service.AccountHoldService;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EndOfDayStep;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class HoldExpirationEodStep implements EndOfDayStep {
    private final AccountHoldService accountHoldService;

    @Autowired
    public HoldExpirationEodStep(AccountHoldService accountHoldService) {
        this.accountHoldService = accountHoldService;
    }

    @Override
    public EodStep getStep() {
        return EodStep.HOLD_EXPIRATION;
    }

    @Override
    public void execute(LocalDate businessDate) {
        String formattedBusinessDate = businessDate.format(DateTimeFormatter.ISO_DATE);

        log.info("Started hold expiration EOD step for business date {}", formattedBusinessDate);
        this.accountHoldService.expireHolds(businessDate);
        log.info("Ended hold expiration EOD step for business date {}", formattedBusinessDate);
    }
}

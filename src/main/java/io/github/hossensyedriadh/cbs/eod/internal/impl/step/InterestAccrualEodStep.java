package io.github.hossensyedriadh.cbs.eod.internal.impl.step;

import io.github.hossensyedriadh.cbs.account.api.service.InterestAccrualService;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EndOfDayStep;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class InterestAccrualEodStep implements EndOfDayStep {
    private final InterestAccrualService interestAccrualService;

    @Autowired
    public InterestAccrualEodStep(InterestAccrualService interestAccrualService) {
        this.interestAccrualService = interestAccrualService;
    }

    @Override
    public EodStep getStep() {
        return EodStep.INTEREST_ACCRUAL;
    }

    @Override
    @Transactional
    public void execute(LocalDate businessDate) {
        String formattedBusinessDate = businessDate.format(DateTimeFormatter.ISO_DATE);

        log.info("Started interest accrual EOD step for business date {}", formattedBusinessDate);
        this.interestAccrualService.accrue(businessDate);
        log.info("Ended interest accrual EOD step for business date {}", formattedBusinessDate);
    }
}

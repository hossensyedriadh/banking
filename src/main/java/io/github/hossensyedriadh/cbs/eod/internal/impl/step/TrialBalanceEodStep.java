package io.github.hossensyedriadh.cbs.eod.internal.impl.step;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EndOfDayStep;
import io.github.hossensyedriadh.cbs.ledger.api.model.TrialBalanceResult;
import io.github.hossensyedriadh.cbs.ledger.api.service.TrialBalanceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class TrialBalanceEodStep implements EndOfDayStep {
    private final TrialBalanceService trialBalanceService;

    @Autowired
    public TrialBalanceEodStep(TrialBalanceService trialBalanceService) {
        this.trialBalanceService = trialBalanceService;
    }

    @Override
    public EodStep getStep() {
        return EodStep.TRIAL_BALANCE;
    }

    @Override
    public void execute(LocalDate businessDate) {
        String formattedBusinessDate = businessDate.format(DateTimeFormatter.ISO_DATE);

        log.info("Started trial balance check EOD step for business date {}", formattedBusinessDate);

        TrialBalanceResult result = this.trialBalanceService.generate();

        log.info("Trial balance result for business date {}.\nTotal Debits: {}, Total Credits: {}, Difference: {}", formattedBusinessDate,
                result.getTotalDebits(), result.getTotalCredits(), result.getDifference());

        log.info("Ended trial balance check EOD step for business date {}", formattedBusinessDate);
    }
}

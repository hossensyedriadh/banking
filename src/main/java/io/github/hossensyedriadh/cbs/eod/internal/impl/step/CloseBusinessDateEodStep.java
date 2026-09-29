package io.github.hossensyedriadh.cbs.eod.internal.impl.step;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.api.service.BusinessDateService;
import io.github.hossensyedriadh.cbs.eod.internal.service.EndOfDayStep;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class CloseBusinessDateEodStep implements EndOfDayStep {
    private final BusinessDateService businessDateService;

    @Autowired
    public CloseBusinessDateEodStep(BusinessDateService businessDateService) {
        this.businessDateService = businessDateService;
    }

    @Override
    public EodStep getStep() {
        return EodStep.CLOSE_BUSINESS_DATE;
    }

    @Override
    public void execute(LocalDate businessDate) {
        String formattedBusinessDate = businessDate.format(DateTimeFormatter.ISO_DATE);

        log.info("Started business date rollover EOD step for business date {}", formattedBusinessDate);
        this.businessDateService.rolloverBusinessDate();
        log.info("Ended business date rollover EOD step for business date {}", formattedBusinessDate);
    }
}

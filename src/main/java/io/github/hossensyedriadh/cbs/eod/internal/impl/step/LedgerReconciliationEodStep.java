package io.github.hossensyedriadh.cbs.eod.internal.impl.step;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EndOfDayStep;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerReconciliationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class LedgerReconciliationEodStep implements EndOfDayStep {
    private final LedgerReconciliationService ledgerReconciliationService;

    @Autowired
    public LedgerReconciliationEodStep(LedgerReconciliationService ledgerReconciliationService) {
        this.ledgerReconciliationService = ledgerReconciliationService;
    }

    @Override
    public EodStep getStep() {
        return EodStep.LEDGER_RECONCILIATION;
    }

    @Override
    public void execute(LocalDate businessDate) {
        String formattedBusinessDate = businessDate.format(DateTimeFormatter.ISO_DATE);

        log.info("Started ledger reconciliation EOD step for business date {}", formattedBusinessDate);
        this.ledgerReconciliationService.reconcile();
        log.info("Ended ledger reconciliation EOD step for business date {}", formattedBusinessDate);
    }
}

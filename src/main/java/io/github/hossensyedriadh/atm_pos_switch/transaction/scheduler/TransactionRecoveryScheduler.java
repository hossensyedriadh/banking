package io.github.hossensyedriadh.atm_pos_switch.transaction.scheduler;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Log4j2
@Service
public class TransactionRecoveryScheduler {
    private final SwitchTransactionService switchTransactionService;

    @Autowired
    public TransactionRecoveryScheduler(SwitchTransactionService switchTransactionService) {
        this.switchTransactionService = switchTransactionService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void recoverStuckTransactions() {
        int recovered = this.switchTransactionService.recoverStuckTransactions(Duration.ofMinutes(5));

        if (recovered > 0) {
            log.info("Recovered {} stuck transactions", recovered);
        }
    }
}

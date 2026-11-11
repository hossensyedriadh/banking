package io.github.hossensyedriadh.atm_pos_switch.transaction.scheduler;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import io.github.hossensyedriadh.atm_pos_switch.util.DistributedLockKeys;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class TransactionRecoveryScheduler {
    private final SwitchTransactionService switchTransactionService;
    private final RedissonClient redissonClient;

    @Autowired
    public TransactionRecoveryScheduler(SwitchTransactionService switchTransactionService, RedissonClient redissonClient) {
        this.switchTransactionService = switchTransactionService;
        this.redissonClient = redissonClient;
    }

    @Scheduled(fixedDelayString = "${switch.stuck-transactions-recovery.fixed-delay}")
    public void recoverStuckTransactions() throws InterruptedException {
        RLock lock = this.redissonClient.getLock(DistributedLockKeys.RECOVER_STUCK_TRANSACTIONS.getKey());

        boolean acquired = lock.tryLock(0, 30, TimeUnit.MINUTES);

        if (!acquired) {
            log.warn("Another instance is already handling stuck transactions recovery lock: {}", DistributedLockKeys.RECOVER_STUCK_TRANSACTIONS.getKey());
            return;
        }

        log.info("Stuck transactions recovery process lock acquired: {}", DistributedLockKeys.RECOVER_STUCK_TRANSACTIONS.getKey());

        try {
            int recovered = this.switchTransactionService.recoverStuckTransactions(Duration.ofMinutes(5));

            if (recovered > 0) {
                log.info("Recovered {} stuck transactions", recovered);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();

                log.info("Stuck transactions recovery process lock released: {}", DistributedLockKeys.RECOVER_STUCK_TRANSACTIONS.getKey());
            }
        }
    }
}

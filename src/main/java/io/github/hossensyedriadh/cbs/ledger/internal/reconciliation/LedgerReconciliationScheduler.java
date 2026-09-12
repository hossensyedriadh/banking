package io.github.hossensyedriadh.cbs.ledger.internal.reconciliation;

import io.github.hossensyedriadh.cbs.ledger.internal.util.DistributedLockKeys;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class LedgerReconciliationScheduler {
    private final LedgerReconciliationServiceImpl ledgerReconciliationServiceImpl;
    private final RedissonClient redissonClient;

    @Autowired
    public LedgerReconciliationScheduler(LedgerReconciliationServiceImpl ledgerReconciliationServiceImpl, RedissonClient redissonClient) {
        this.ledgerReconciliationServiceImpl = ledgerReconciliationServiceImpl;
        this.redissonClient = redissonClient;
    }

    @Async("virtualThreadExecutor")
    @Scheduled(cron = "0 0 */6 * * *")
    public void reconcile() throws InterruptedException {
        log.info("Ledger Reconciliation job started...");

        RLock lock = this.redissonClient.getLock(DistributedLockKeys.LEDGER_RECONCILE.getKey());
        boolean ack = lock.tryLock(0, 10, TimeUnit.MINUTES);

        if (!ack) {
            log.warn("Failed to acquire lock, another instance is holding lock for key: {}.", DistributedLockKeys.LEDGER_RECONCILE.getKey());
            return;
        }

        log.info("Lock acquired for job: {}", DistributedLockKeys.LEDGER_RECONCILE.getKey());

        try {
            this.ledgerReconciliationServiceImpl.reconcileAllSubLedgers();

            log.info("Ledger reconciliation job finished.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for job: {}", DistributedLockKeys.LEDGER_RECONCILE.getKey());
            }
        }
    }
}

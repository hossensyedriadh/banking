package io.github.hossensyedriadh.internet_banking.transaction.scheduled;

import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationContext;
import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationType;
import io.github.hossensyedriadh.internet_banking.transaction.service.ChannelTransactionService;
import io.github.hossensyedriadh.internet_banking.transaction.service.TransactionReconciliationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
public class TransactionReconciliationJob {
    private final ChannelTransactionService channelTransactionService;
    private final TransactionReconciliationService transactionReconciliationService;

    @Autowired
    public TransactionReconciliationJob(ChannelTransactionService channelTransactionService,
                                        TransactionReconciliationService transactionReconciliationService) {
        this.channelTransactionService = channelTransactionService;
        this.transactionReconciliationService = transactionReconciliationService;
    }

    @Scheduled(fixedDelayString = "${ibanking.transaction.reconciliation.fixed-delay:300000}",
            initialDelayString = "${ibanking.transaction.reconciliation.initial-delay:60000}")
    public void reconcileUnknownTransactions() {
        log.info("Transactions reconciliation started...");

        GatewayAuthenticationContext.set(GatewayAuthenticationType.SERVICE);

        try {
            List<Long> transactionIds = this.channelTransactionService.findUnknownTransactionIds(100);

            log.info("Found {} unknown transactions", transactionIds.size());

            transactionIds.forEach(transactionId -> {
                try {
                    this.transactionReconciliationService.reconcile(transactionId);
                } catch (Exception e) {
                    log.error("Failed to reconcile transaction [ID: {}]", transactionId, e);
                }
            });
        } finally {
            GatewayAuthenticationContext.clear();
            log.info("Transaction reconciliations finished");
        }
    }
}

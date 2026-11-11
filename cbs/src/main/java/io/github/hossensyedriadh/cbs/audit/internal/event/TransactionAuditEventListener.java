package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.transaction.api.event.TransactionCompletedEvent;
import io.github.hossensyedriadh.cbs.transaction.api.event.TransactionCreatedEvent;
import io.github.hossensyedriadh.cbs.transaction.api.event.TransactionFailedEvent;
import io.github.hossensyedriadh.cbs.transaction.api.event.TransactionReversedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionAuditEventListener {
    private final AuditService auditService;

    private final static String TRANSACTION_NAME = "TRANSACTION";

    @Autowired
    public TransactionAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(TransactionCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(TRANSACTION_NAME, TRANSACTION_NAME, event.transactionId(),
                AuditAction.CREATED, "Transaction type " + event.type().toString() + " with reference "
                + event.reference() + " created"));
    }

    @ApplicationModuleListener
    public void on(TransactionCompletedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(TRANSACTION_NAME, TRANSACTION_NAME, event.transactionId(),
                AuditAction.COMPLETED, "Transaction type " + event.type().toString() + " with reference "
                + event.reference() + " completed"));
    }

    @ApplicationModuleListener
    public void on(TransactionFailedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(TRANSACTION_NAME, TRANSACTION_NAME, event.transactionId(),
                AuditAction.FAILED, "Transaction type " + event.type().toString() + " with reference "
                + event.reference() + " failed"));
    }

    @ApplicationModuleListener
    public void on(TransactionReversedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(TRANSACTION_NAME, TRANSACTION_NAME, event.originalTransactionId(),
                AuditAction.REVERSED, "Transaction type " + event.type().toString() + " with reference "
                + event.reference() + " reversed. Reversal ID: " + event.reversalTransactionId()));
    }
}

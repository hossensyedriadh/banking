package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.ledger.api.event.ledger.DepositPostedEvent;
import io.github.hossensyedriadh.cbs.ledger.api.event.ledger.SubLedgerAccountCreatedEvent;
import io.github.hossensyedriadh.cbs.ledger.api.event.ledger.TransferPostedEvent;
import io.github.hossensyedriadh.cbs.ledger.api.event.ledger.WithdrawalPostedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class LedgerAuditEventListener {
    private final AuditService auditService;

    private final static String LEDGER_NAME = "LEDGER";
    private final static String LEDGER_ACCOUNT_NAME = "LEDGER_ACCOUNT";

    @Autowired
    public LedgerAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(SubLedgerAccountCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_ACCOUNT_NAME, LEDGER_ACCOUNT_NAME, event.subLedgerId(),
                AuditAction.CREATED, "SubLedger Account for ledger " + event.ledgerAccountCode() + " created"));
    }

    @ApplicationModuleListener
    public void on(DepositPostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_NAME, event.transactionId(),
                AuditAction.POSTED, "Deposit posted for transaction " + event.transactionId()));
    }

    @ApplicationModuleListener
    public void on(WithdrawalPostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_NAME, event.transactionId(),
                AuditAction.POSTED, "Withdrawal posted for transaction " + event.transactionId()));
    }

    @ApplicationModuleListener
    public void on(TransferPostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_NAME, event.transactionId(),
                AuditAction.POSTED, "Transfer posted for transaction " + event.transactionId()));
    }
}

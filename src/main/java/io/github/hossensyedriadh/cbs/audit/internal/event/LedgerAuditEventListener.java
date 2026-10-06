package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.ledger.api.event.ledger.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class LedgerAuditEventListener {
    private final AuditService auditService;

    private final static String LEDGER_NAME = "LEDGER";
    private final static String LEDGER_ACCOUNT_NAME = "LEDGER_ACCOUNT";
    private final static String SUB_LEDGER_ACCOUNT_NAME = "SUB_LEDGER_ACCOUNT";
    private final static String LEDGER_ENTITY_NAME = "LEDGER";

    @Autowired
    public LedgerAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(LedgerAccountCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_ACCOUNT_NAME, LEDGER_ACCOUNT_NAME, event.ledgerAccountId(),
                AuditAction.CREATED, "Ledger Account created with code: " + event.accountCode()));
    }

    @ApplicationModuleListener
    public void on(LedgerAccountUpdatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_ACCOUNT_NAME, LEDGER_ACCOUNT_NAME, event.ledgerAccountId(),
                AuditAction.UPDATED, "Ledger Account updated with code: " + event.accountCode()));
    }

    @ApplicationModuleListener
    public void on(LedgerAccountActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_ACCOUNT_NAME, LEDGER_ACCOUNT_NAME, event.ledgerAccountId(),
                AuditAction.ACTIVATED, "Ledger Account activated with code: " + event.accountCode()));
    }

    @ApplicationModuleListener
    public void on(LedgerAccountDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_ACCOUNT_NAME, LEDGER_ACCOUNT_NAME, event.ledgerAccountId(),
                AuditAction.DEACTIVATED, "Ledger Account deactivated with code: " + event.accountCode()));
    }

    @ApplicationModuleListener
    public void on(SubLedgerAccountCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_ACCOUNT_NAME, SUB_LEDGER_ACCOUNT_NAME, event.subLedgerId(),
                AuditAction.CREATED, "SubLedger Account for ledger " + event.ledgerAccountCode() + " created"));
    }

    @ApplicationModuleListener
    public void on(DepositPostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_ENTITY_NAME, event.transactionId(),
                AuditAction.POSTED, "Deposit posted for transaction " + event.transactionId()));
    }

    @ApplicationModuleListener
    public void on(WithdrawalPostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_ENTITY_NAME, event.transactionId(),
                AuditAction.POSTED, "Withdrawal posted for transaction " + event.transactionId()));
    }

    @ApplicationModuleListener
    public void on(TransferPostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_ENTITY_NAME, event.transactionId(),
                AuditAction.POSTED, "Transfer posted for transaction " + event.transactionId()));
    }

    @ApplicationModuleListener
    public void on(InterestPostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_ENTITY_NAME, event.accrualId(),
                AuditAction.POSTED, "Interest posted for accrual " + event.accrualId()));
    }

    @ApplicationModuleListener
    public void on(FeePostedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(LEDGER_NAME, LEDGER_ENTITY_NAME, event.transactionId(),
                AuditAction.POSTED, "Fee posted for transaction " + event.transactionId()));
    }
}

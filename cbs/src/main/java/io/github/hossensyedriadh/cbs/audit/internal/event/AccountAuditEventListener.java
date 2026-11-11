package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.account.api.event.*;
import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class AccountAuditEventListener {
    private final AuditService auditService;

    private final static String MODULE_NAME = "ACCOUNT";
    private final static String ACCOUNT_ENTITY_NAME = "ACCOUNT";
    private final static String HOLD_ENTITY_NAME = "HOLD";
    private final static String RESTRICTION_ENTITY_NAME = "RESTRICTION";

    @Autowired
    public AccountAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(AccountCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ACCOUNT_ENTITY_NAME, event.accountId(),
                AuditAction.CREATED, "Account " + event.accountNumber() + " created"));
    }

    @ApplicationModuleListener
    public void on(AccountClosedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ACCOUNT_ENTITY_NAME, event.accountId(),
                AuditAction.CLOSED, "Account " + event.accountNumber() + " closed, reason: " + event.reason()));
    }

    @ApplicationModuleListener
    public void on(AccountActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ACCOUNT_ENTITY_NAME, event.accountId(),
                AuditAction.ACTIVATED, "Account " + event.accountNumber() + " activated"));
    }

    @ApplicationModuleListener
    public void on(AccountDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ACCOUNT_ENTITY_NAME, event.accountId(),
                AuditAction.DEACTIVATED, "Account " + event.accountNumber() + " deactivated"));
    }

    @ApplicationModuleListener
    public void on(AccountHoldCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, HOLD_ENTITY_NAME, event.holdId(),
                AuditAction.CREATED, "Balance Hold created, reason: " + event.reason()));
    }

    @ApplicationModuleListener
    public void on(AccountHoldReleasedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, HOLD_ENTITY_NAME, event.holdId(),
                AuditAction.RELEASED, "Balance Hold released, reason: " + event.reason()));
    }

    @ApplicationModuleListener
    public void on(AccountRestrictionCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, RESTRICTION_ENTITY_NAME, event.restrictionId(),
                AuditAction.CREATED, "Account restriction created, reason: " + event.reason()));
    }

    @ApplicationModuleListener
    public void on(AccountRestrictionReleasedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, RESTRICTION_ENTITY_NAME, event.restrictionId(),
                AuditAction.RELEASED, "Account restriction released, reason: " + event.reason()));
    }
}

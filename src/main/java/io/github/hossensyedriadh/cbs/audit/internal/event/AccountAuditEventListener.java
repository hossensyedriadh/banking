package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.account.api.event.AccountBlockedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.AccountClosedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.AccountCreatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.AccountUnblockedEvent;
import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class AccountAuditEventListener {
    private final AuditService auditService;

    private final static String NAME = "ACCOUNT";

    @Autowired
    public AccountAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(AccountCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.accountId(),
                AuditAction.CREATED, "Account " + event.accountNumber() + " created"));
    }

    @ApplicationModuleListener
    public void on(AccountBlockedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.accountId(),
                AuditAction.BLOCKED, "Account " + event.accountNumber() + " blocked"));
    }

    @ApplicationModuleListener
    public void on(AccountUnblockedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.accountId(),
                AuditAction.UNBLOCKED, "Account " + event.accountNumber() + " unblocked"));
    }

    @ApplicationModuleListener
    public void on(AccountClosedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.accountId(),
                AuditAction.CLOSED, "Account " + event.accountNumber() + " closed"));
    }
}

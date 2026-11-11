package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchActivatedEvent;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchCreatedEvent;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchDeactivatedEvent;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class BranchAuditEventListener {
    private final AuditService auditService;

    private final static String NAME = "BRANCH";

    @Autowired
    public BranchAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(BranchCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.branchId(),
                AuditAction.CREATED, "Branch " + event.branchCode() + " created"));
    }

    @ApplicationModuleListener
    public void on(BranchUpdatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.branchId(),
                AuditAction.UPDATED, "Branch " + event.branchCode() + " updated"));
    }

    @ApplicationModuleListener
    public void on(BranchActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.branchId(),
                AuditAction.ACTIVATED, "Branch " + event.branchCode() + " activated"));
    }

    @ApplicationModuleListener
    public void on(BranchDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.branchId(),
                AuditAction.DEACTIVATED, "Branch " + event.branchCode() + " deactivated"));
    }
}

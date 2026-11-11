package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.account.api.event.FeeRuleActivatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.FeeRuleCreatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.FeeRuleDeactivatedEvent;
import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class FeeRuleAuditEventListener {
    private final AuditService auditService;

    private final static String MODULE_NAME = "ACCOUNT";
    private final static String ENTITY_NAME = "FEE_RULE";

    @Autowired
    public FeeRuleAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(FeeRuleCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ENTITY_NAME, event.id(),
                AuditAction.CREATED, "Fee rule created with code: " + event.feeCode()));
    }

    @ApplicationModuleListener
    public void on(FeeRuleActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ENTITY_NAME, event.id(),
                AuditAction.ACTIVATED, "Fee rule activated with code: " + event.feeCode()));
    }

    @ApplicationModuleListener
    public void on(FeeRuleDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ENTITY_NAME, event.id(),
                AuditAction.DEACTIVATED, "Fee rule deactivated with code: " + event.feeCode()));
    }
}

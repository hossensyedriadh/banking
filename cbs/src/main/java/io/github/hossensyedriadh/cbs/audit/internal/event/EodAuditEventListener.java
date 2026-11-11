package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.eod.api.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class EodAuditEventListener {
    private final AuditService auditService;

    private final static String MODULE_NAME = "EOD";
    private final static String EOD_RUN_ENTITY_NAME = "EOD_RUN";
    private final static String EOD_RUN_STEP_ENTITY_NAME = "EOD_RUN_STEP";

    @Autowired
    public EodAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(EodRunStartedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, EOD_RUN_ENTITY_NAME, event.eodRunId(),
                AuditAction.STARTED, "EOD run started for business date: " + event.businessDate()));
    }

    @ApplicationModuleListener
    public void on(EodRunCompletedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, EOD_RUN_ENTITY_NAME, event.eodRunId(),
                AuditAction.COMPLETED, "EOD run completed for business date: " + event.businessDate()));
    }

    @ApplicationModuleListener
    public void on(EodRunFailedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, EOD_RUN_ENTITY_NAME, event.eodRunId(),
                AuditAction.FAILED, "EOD run failed for business date: " + event.businessDate()));
    }

    @ApplicationModuleListener
    public void on(EodRunStepStartedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, EOD_RUN_STEP_ENTITY_NAME, event.eodRunStepId(),
                AuditAction.STARTED, "EOD run step: " + event.step() + " started"));
    }

    @ApplicationModuleListener
    public void on(EodRunStepCompletedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, EOD_RUN_STEP_ENTITY_NAME, event.eodRunStepId(),
                AuditAction.COMPLETED, "EOD run step: " + event.step() + " completed"));
    }

    @ApplicationModuleListener
    public void on(EodRunStepFailedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, EOD_RUN_STEP_ENTITY_NAME, event.eodRunStepId(),
                AuditAction.FAILED, "EOD run step: " + event.step() + " failed"));
    }
}

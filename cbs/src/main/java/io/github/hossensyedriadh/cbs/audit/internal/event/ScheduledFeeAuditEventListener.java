package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeCancelledEvent;
import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeCreatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeProcessedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeWaivedEvent;
import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ScheduledFeeAuditEventListener {
    private final AuditService auditService;

    private final static String MODULE_NAME = "ACCOUNT";
    private final static String ENTITY_NAME = "SCHEDULED_FEE";

    @Autowired
    public ScheduledFeeAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(ScheduledFeeCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ENTITY_NAME, event.id(),
                AuditAction.SCHEDULED, "Scheduled fee scheduled for business date: "
                + event.scheduledDate().format(DateTimeFormatter.ISO_DATE)));
    }

    @ApplicationModuleListener
    public void on(ScheduledFeeProcessedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ENTITY_NAME, event.id(),
                AuditAction.PROCESSED, "Scheduled fee processed for business date: "
                + event.scheduledDate().format(DateTimeFormatter.ISO_DATE)));
    }

    @ApplicationModuleListener
    public void on(ScheduledFeeCancelledEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ENTITY_NAME, event.id(),
                AuditAction.CANCELLED, "Scheduled fee cancelled for business date: "
                + event.scheduledDate().format(DateTimeFormatter.ISO_DATE)));
    }

    @ApplicationModuleListener
    public void on(ScheduledFeeWaivedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ENTITY_NAME, event.id(),
                AuditAction.WAIVED, "Scheduled fee waived for business date: "
                + event.scheduledDate().format(DateTimeFormatter.ISO_DATE)));
    }
}

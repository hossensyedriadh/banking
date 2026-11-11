package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.statement.api.event.MonthlyStatementGeneratedEvent;
import io.github.hossensyedriadh.cbs.statement.api.event.OnDemandStatementGeneratedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class StatementAuditEventListener {
    private final AuditService auditService;

    private final static String MODULE_NAME = "STATEMENT";
    private final static String MONTHLY_STMT_ENTITY_NAME = "MONTHLY";
    private final static String ON_DEMAND_STMT_ENTITY_NAME = "ON_DEMAND";

    @Autowired
    public StatementAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(MonthlyStatementGeneratedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, MONTHLY_STMT_ENTITY_NAME, event.statementId(),
                AuditAction.CREATED, "Monthly statement created for period: " + event.period()));
    }

    @ApplicationModuleListener
    public void on(OnDemandStatementGeneratedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(MODULE_NAME, ON_DEMAND_STMT_ENTITY_NAME, event.statementId(),
                AuditAction.CREATED, "On-demand statement created for date range [from: " + event.from().format(DateTimeFormatter.ISO_DATE_TIME)
                + " to: " + event.to().format(DateTimeFormatter.ISO_DATE_TIME) + "]"));
    }
}

package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.customer.api.event.CustomerCreatedEvent;
import io.github.hossensyedriadh.cbs.customer.api.event.CustomerUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerAuditEventListener {
    private final AuditService auditService;

    private final static String NAME = "CUSTOMER";

    @Autowired
    public CustomerAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(CustomerCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.customerId(),
                AuditAction.CREATED, "Customer " + event.customerId() + " created"));
    }

    @ApplicationModuleListener
    public void on(CustomerUpdatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.customerId(),
                AuditAction.UPDATED, "Customer " + event.customerId() + " updated"));
    }
}

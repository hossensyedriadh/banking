package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductActivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductCreatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductDeactivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductUpdatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.currency.CurrencyActivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.currency.CurrencyCreatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.currency.CurrencyDeactivatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class ProductAuditEventListener {
    private final AuditService auditService;

    private final static String ACCOUNT_PRODUCT_AUDIT_NAME = "ACCOUNT_PRODUCT";
    private final static String CURRENCY_PRODUCT_AUDIT_NAME = "CURRENCY";

    @Autowired
    public ProductAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(AccountProductCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(ACCOUNT_PRODUCT_AUDIT_NAME, ACCOUNT_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.CREATED, "Account product " + event.productCode() + " created"));
    }

    @ApplicationModuleListener
    public void on(AccountProductUpdatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(ACCOUNT_PRODUCT_AUDIT_NAME, ACCOUNT_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.UPDATED, "Account product " + event.productCode() + " updated"));
    }

    @ApplicationModuleListener
    public void on(AccountProductActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(ACCOUNT_PRODUCT_AUDIT_NAME, ACCOUNT_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.ACTIVATED, "Account product " + event.productCode() + " activated"));
    }

    @ApplicationModuleListener
    public void on(AccountProductDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(ACCOUNT_PRODUCT_AUDIT_NAME, ACCOUNT_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.DEACTIVATED, "Account product " + event.productCode() + " deactivated"));
    }

    @ApplicationModuleListener
    public void on(CurrencyCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CURRENCY_PRODUCT_AUDIT_NAME, CURRENCY_PRODUCT_AUDIT_NAME, event.currencyId(),
                AuditAction.CREATED, "Currency " + event.currencyCode() + " created"));
    }

    @ApplicationModuleListener
    public void on(CurrencyActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CURRENCY_PRODUCT_AUDIT_NAME, CURRENCY_PRODUCT_AUDIT_NAME, event.currencyId(),
                AuditAction.ACTIVATED, "Currency " + event.currencyCode() + " activated"));
    }

    @ApplicationModuleListener
    public void on(CurrencyDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CURRENCY_PRODUCT_AUDIT_NAME, CURRENCY_PRODUCT_AUDIT_NAME, event.currencyId(),
                AuditAction.DEACTIVATED, "Currency " + event.currencyCode() + " deactivated"));
    }
}

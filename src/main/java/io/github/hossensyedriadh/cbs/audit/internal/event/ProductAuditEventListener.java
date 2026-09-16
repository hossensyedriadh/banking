package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductActivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductCreatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductDeactivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.accountproduct.AccountProductUpdatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.cardproduct.CardProductActivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.cardproduct.CardProductCreatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.cardproduct.CardProductDeactivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.cardproduct.CardProductUpdatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductActivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductCreatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductDeactivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductUpdatedEvent;
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
    private final static String CARD_PRODUCT_AUDIT_NAME = "CARD_PRODUCT";
    private final static String CHEQUE_PRODUCT_AUDIT_NAME = "CHEQUE_PRODUCT";
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

    @ApplicationModuleListener
    public void on(CardProductCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CARD_PRODUCT_AUDIT_NAME, CARD_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.CREATED, "Card product " + event.productCode() + " created"));
    }

    @ApplicationModuleListener
    public void on(CardProductUpdatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CARD_PRODUCT_AUDIT_NAME, CARD_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.UPDATED, "Card product " + event.productCode() + " updated"));
    }

    @ApplicationModuleListener
    public void on(CardProductActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CARD_PRODUCT_AUDIT_NAME, CARD_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.ACTIVATED, "Card product " + event.productCode() + " activated"));
    }

    @ApplicationModuleListener
    public void on(CardProductDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CARD_PRODUCT_AUDIT_NAME, CARD_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.DEACTIVATED, "Card product " + event.productCode() + " deactivated"));
    }

    @ApplicationModuleListener
    public void on(ChequeProductCreatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CHEQUE_PRODUCT_AUDIT_NAME, CHEQUE_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.CREATED, "Cheque product " + event.productCode() + " created"));
    }

    @ApplicationModuleListener
    public void on(ChequeProductUpdatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CHEQUE_PRODUCT_AUDIT_NAME, CHEQUE_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.UPDATED, "Cheque product " + event.productCode() + " updated"));
    }

    @ApplicationModuleListener
    public void on(ChequeProductActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CHEQUE_PRODUCT_AUDIT_NAME, CHEQUE_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.ACTIVATED, "Cheque product " + event.productCode() + " activated"));
    }

    @ApplicationModuleListener
    public void on(ChequeProductDeactivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(CHEQUE_PRODUCT_AUDIT_NAME, CHEQUE_PRODUCT_AUDIT_NAME, event.productId(),
                AuditAction.DEACTIVATED, "Cheque product " + event.productCode() + " deactivated"));
    }
}

package io.github.hossensyedriadh.cbs.audit.internal.event;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.card.api.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class CardAuditEventListener {
    private final AuditService auditService;

    private final static String NAME = "CARD";

    @Autowired
    public CardAuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApplicationModuleListener
    public void on(CardIssuedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.cardId(),
                AuditAction.ISSUED, "Card " + event.cardId() + " issued for account " + event.accountId()));
    }

    @ApplicationModuleListener
    public void on(CardActivatedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.cardId(),
                AuditAction.ACTIVATED, "Card " + event.cardId() + " activated"));
    }

    @ApplicationModuleListener
    public void on(CardTemporaryBlockedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.cardId(),
                AuditAction.BLOCKED, "Card " + event.cardId() + " temporarily blocked, reason: " + event.reason()));
    }

    @ApplicationModuleListener
    public void on(CardPermanentBlockedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.cardId(),
                AuditAction.BLOCKED, "Card " + event.cardId() + " permanently blocked, reason: " + event.reason()));
    }

    @ApplicationModuleListener
    public void on(CardUnblockedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.cardId(),
                AuditAction.UNBLOCKED, "Card " + event.cardId() + " unblocked, reason: " + event.reason()));
    }

    @ApplicationModuleListener
    public void on(CardClosedEvent event) {
        this.auditService.record(new CreateAuditLogCommand(NAME, NAME, event.cardId(),
                AuditAction.CLOSED, "Card " + event.cardId() + " closed, reason: " + event.reason()));
    }
}

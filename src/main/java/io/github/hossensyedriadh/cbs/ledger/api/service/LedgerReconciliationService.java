package io.github.hossensyedriadh.cbs.ledger.api.service;

import io.github.hossensyedriadh.cbs.ledger.api.model.ReconciliationResult;

public interface LedgerReconciliationService {
    ReconciliationResult reconcileSubLedgerWithGL(Long glAccountId);

    void reconcile();
}

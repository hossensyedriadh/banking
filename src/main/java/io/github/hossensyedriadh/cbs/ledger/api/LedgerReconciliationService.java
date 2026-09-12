package io.github.hossensyedriadh.cbs.ledger.api;

import io.github.hossensyedriadh.cbs.ledger.api.model.ReconciliationResult;

public interface LedgerReconciliationService {
    ReconciliationResult reconcileSubLedgerWithGL(Long glAccountId);
}

package io.github.hossensyedriadh.cbs.ledger.internal.projection;

import java.math.BigDecimal;

public record LedgerReconciliationResult(Long ledgerAccountId, String ledgerAccountCode, BigDecimal glBalance,
                                         BigDecimal subLedgerBalance, BigDecimal difference) {
}

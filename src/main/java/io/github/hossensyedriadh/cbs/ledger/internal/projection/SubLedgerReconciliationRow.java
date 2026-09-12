package io.github.hossensyedriadh.cbs.ledger.internal.projection;

import java.math.BigDecimal;

public record SubLedgerReconciliationRow(Long glAccountId, String accountCode, BigDecimal glBalance,
                                         BigDecimal subLedgerBalance) {
}

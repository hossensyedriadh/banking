package io.github.hossensyedriadh.atm_terminal.model;

import java.math.BigDecimal;

public record FundTransferResult(boolean successful, String destinationAccount, BigDecimal amount, String currency,
                                 String responseCode, String message) {
}

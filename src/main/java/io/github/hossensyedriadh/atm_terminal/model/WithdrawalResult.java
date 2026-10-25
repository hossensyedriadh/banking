package io.github.hossensyedriadh.atm_terminal.model;

import java.math.BigDecimal;

public record WithdrawalResult(boolean successful, BigDecimal amount, String currency,
                               String responseCode, String message) {
}

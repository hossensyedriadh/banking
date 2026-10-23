package io.github.hossensyedriadh.atm_terminal.model;

import java.math.BigDecimal;

public record BalanceInquiryResult(boolean successful, BigDecimal availableBalance, String currency, String message) {
}
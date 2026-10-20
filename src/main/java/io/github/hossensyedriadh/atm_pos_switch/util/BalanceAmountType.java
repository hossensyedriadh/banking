package io.github.hossensyedriadh.atm_pos_switch.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BalanceAmountType {
    LEDGER("01"),
    AVAILABLE("02"),
    OUTSTANDING("03"),
    OVERDRAFT("04");

    private final String de54Code;
}

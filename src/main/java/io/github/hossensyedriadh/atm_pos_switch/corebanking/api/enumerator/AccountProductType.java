package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountProductType {
    SAVINGS ("10"),
    CURRENT ("20"),
    FIXED_DEPOSIT ("40");

    private final String de54Code;
}

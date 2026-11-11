package io.github.hossensyedriadh.internet_banking.account.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class AccountBalanceModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -4782589590615619048L;

    private BigDecimal availableBalance;

    private BigDecimal ledgerBalance;
}

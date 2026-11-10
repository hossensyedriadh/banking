package io.github.hossensyedriadh.internet_banking.account.model;

import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountOverviewModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -6077439021751685236L;

    private Long accountId;

    private String accountNumber;

    private AccountStatus status;

    private OffsetDateTime openedAt;

    private AccountBalanceModel balance;

    private CurrencyModel currency;

    private AccountProductModel accountProduct;
}

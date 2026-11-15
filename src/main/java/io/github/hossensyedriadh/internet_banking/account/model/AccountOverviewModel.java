package io.github.hossensyedriadh.internet_banking.account.model;

import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountStatus;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountOverviewModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -6077439021751685236L;

    private Long accountId;

    private String accountNumber;

    private AccountStatus status;

    private OffsetDateTime openedAt;

    private AccountBalanceModel balance;

    private CurrencyModel currency;

    private BranchModel branch;

    private AccountProductModel accountProduct;
}

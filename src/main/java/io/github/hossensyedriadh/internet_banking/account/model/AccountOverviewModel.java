package io.github.hossensyedriadh.internet_banking.account.model;

import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountStatus;
import io.github.hossensyedriadh.internet_banking.common.AccountModel;
import io.github.hossensyedriadh.internet_banking.common.BranchModel;
import io.github.hossensyedriadh.internet_banking.common.CurrencyModel;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountOverviewModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -6077439021751685236L;

    private AccountModel account;

    private AccountBalanceModel balance;

    private CurrencyModel currency;

    private BranchModel branch;

    private AccountProductModel accountProduct;
}

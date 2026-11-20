package io.github.hossensyedriadh.internet_banking.common;

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
public class AccountModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 8675489722926036614L;

    private Long accountId;

    private String accountNumber;

    private AccountStatus status;

    private OffsetDateTime openedAt;
}

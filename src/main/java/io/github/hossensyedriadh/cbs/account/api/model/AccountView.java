package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountView implements Serializable {
    @Serial
    private static final long serialVersionUID = 4272680252032185354L;

    private Long id;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("currency_id")
    private Long currencyId;

    private AccountStatus status;

    @JsonProperty("opened_at")
    private OffsetDateTime openedAt;

    @JsonProperty("closed_at")
    private OffsetDateTime closedAt;
}

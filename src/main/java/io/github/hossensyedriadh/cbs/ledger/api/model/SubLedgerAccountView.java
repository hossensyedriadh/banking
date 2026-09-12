package io.github.hossensyedriadh.cbs.ledger.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class SubLedgerAccountView implements Serializable {
    @Serial
    private static final long serialVersionUID = -857637341570568226L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("ledger_account_id")
    private Long ledgerAccountId;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}

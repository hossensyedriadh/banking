package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountHoldView implements Serializable {
    @Serial
    private static final long serialVersionUID = -3926179939154988173L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    private BigDecimal amount;

    private AccountHoldStatus status;

    private String reason;

    private String reference;

    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("released_at")
    private OffsetDateTime releasedAt;
}

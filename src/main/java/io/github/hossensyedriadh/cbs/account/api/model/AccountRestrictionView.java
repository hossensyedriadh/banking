package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountRestrictionView implements Serializable {
    @Serial
    private static final long serialVersionUID = -4353648630726891544L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("restriction_type")
    private AccountRestrictionType restrictionType;

    private AccountRestrictionStatus status;

    private String reason;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("released_at")
    private OffsetDateTime releasedAt;
}

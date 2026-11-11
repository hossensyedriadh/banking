package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

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

    private List<AccountRestrictionStatusHistoryView> history;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}

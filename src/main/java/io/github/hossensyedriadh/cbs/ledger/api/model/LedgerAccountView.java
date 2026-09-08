package io.github.hossensyedriadh.cbs.ledger.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.ledger.api.enumerator.LedgerAccountType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class LedgerAccountView implements Serializable {
    @Serial
    private static final long serialVersionUID = 6753895446453419005L;

    private Long id;

    @JsonProperty("account_code")
    private String accountCode;

    private String name;

    @JsonProperty("currency_id")
    private Long currencyId;

    @JsonProperty("account_type")
    private LedgerAccountType accountType;

    private boolean active;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}

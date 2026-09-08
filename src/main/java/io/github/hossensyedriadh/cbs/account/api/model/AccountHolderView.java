package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHolderType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountHolderView implements Serializable {
    @Serial
    private static final long serialVersionUID = 4491290145556572564L;

    private AccountHolderIdView id;

    private AccountView account;

    @JsonProperty("holder_type")
    private AccountHolderType holderType;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}

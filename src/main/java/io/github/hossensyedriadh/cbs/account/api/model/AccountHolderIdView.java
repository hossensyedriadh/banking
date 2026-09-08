package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class AccountHolderIdView implements Serializable {
    @Serial
    private static final long serialVersionUID = -5242186116390897204L;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("customer_id")
    private Long customerId;
}
